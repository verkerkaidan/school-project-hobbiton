package com.recognitionservice.services;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.TranslateException;
import com.recognitionservice.enums.ContentFlag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
public class RecognitionService {

    @Value("${ai.model.path}")
    private String modelPath;

    // Class index 0 → SAFE, class index 1 → RISK.
    // Swap the order if your training labels were defined differently.
    private static final List<String> CLASS_NAMES = List.of("SAFE", "RISK");

    // Standard ImageNet normalization values (used when ResNet50 was pre-trained)
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD  = {0.229f, 0.224f, 0.225f};

    private Model model;
    private Predictor<Image, Classifications> predictor;

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    @PostConstruct
    public void init() throws IOException, ModelException {

        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(256))
                .addTransform(new CenterCrop(224, 224))
                .addTransform(new ToTensor())
                .addTransform(new Normalize(MEAN, STD))
                .optApplySoftmax(true)
                .optSynset(CLASS_NAMES)
                .build();

        model = Model.newInstance("car-model");

        // Load externally downloaded model file
        model.load(Paths.get(modelPath));

        predictor = model.newPredictor(translator);
    }

    @PreDestroy
    public void destroy() {
        if (predictor != null) predictor.close();
        if (model != null)     model.close();
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Assesses the risk factor of a given file using the ResNet50 model.
     *
     * @param fileData image bytes
     * @return ContentFlag.SAFE or ContentFlag.RISK
     */
    public ContentFlag flagFile(byte[] fileData) {
        try {
            // Normalize to PNG so DJL's BufferedImageFactory always succeeds,
            // regardless of whether the input was WebP, JPEG, PNG, GIF, etc.
            byte[] normalized = normalizeImageBytes(fileData);

            Image image = ImageFactory.getInstance()
                    .fromInputStream(new ByteArrayInputStream(normalized));

            Classifications result = predictor.predict(image);
            String topLabel = result.best().getClassName();

            return switch (topLabel) {
                case "SAFE" -> ContentFlag.SAFE;
                case "RISK" -> ContentFlag.RISK;
                default -> throw new RuntimeException("Unexpected label from model: " + topLabel);
            };

        } catch (TranslateException | IOException e) {
            throw new RuntimeException("Failed to run inference on image", e);
        }
    }

    /**
     * Re-encodes image bytes to PNG if the format isn't natively supported by
     * Java ImageIO (e.g. WebP without a plugin). With TwelveMonkeys on the
     * classpath this also acts as a validation step.
     *
     * @param fileData raw image bytes (any format)
     * @return PNG-encoded bytes that ImageIO / DJL can always read
     * @throws IOException if the bytes cannot be decoded as any known image format
     */
    private byte[] normalizeImageBytes(byte[] fileData) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(fileData));

        if (bufferedImage == null) {
            throw new IOException(
                    "ImageIO could not decode the supplied bytes. " +
                            "Unsupported or corrupt image format."
            );
        }

        // If the image has no alpha channel, use RGB to avoid PNG palette issues
        // with some DJL backends.
        if (bufferedImage.getType() == BufferedImage.TYPE_BYTE_INDEXED
                || bufferedImage.getType() == BufferedImage.TYPE_CUSTOM) {
            BufferedImage rgb = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR
            );
            rgb.getGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = rgb;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", out);
        return out.toByteArray();
    }
}
