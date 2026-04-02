import React from "react";

// Types (gelijk aan ResultsList)
type BookResult = {
  title: string;
  description?: string;
  authors?: string[];
  publisher?: string[];
  publishedDate?: string;
  isbn?: string[];
  pageCount?: string;
  language?: string[];
  coverId?: string;
  thumbnailUrl?: string;
  resultType: "book";
};
type ExerciseResult = {
  exerciseId: string;
  name: string;
  overview?: string;
  instructions?: string[];
  gifUrl?: string;
  bodyParts?: string[];
  targetMuscles?: string[];
  secondaryMuscles?: string[];
  equipments?: string[];
  resultType: "exercise";
};
type MovieResult = {
  movieId: string;
  title: string;
  description?: string;
  poster?: string;
  year?: string;
  mediaType?: string;
  resultType: "movie";
};
type ResultItem = BookResult | ExerciseResult | MovieResult;

export default function ResultDetailModal({
  result,
  onClose,
}: {
  result: ResultItem;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
      <div className="glass-card rounded-2xl border border-white/10 shadow-2xl max-w-lg w-full p-6 relative animate-fade-in backdrop-blur-2xl"
           style={{ boxShadow: "0 25px 50px -12px var(--color-brand-muted, rgba(236,72,153,0.1))" }}>
        <button
          className="absolute top-3 right-3 text-2xl text-gray-400 hover:text-brand focus:outline-none"
          onClick={onClose}
          aria-label="Close"
        >
          ×
        </button>
        {result.resultType === "book" && (
          <div>
            <div className="flex gap-4 mb-4">
              {result.thumbnailUrl && (
                <img src={result.thumbnailUrl} alt={result.title} className="w-24 h-32 object-cover rounded-lg border" />
              )}
              <div>
                <h2 className="text-2xl font-bold mb-1">{result.title}</h2>
                {result.authors && <div className="text-gray-600 mb-1">By {result.authors.join(", ")}</div>}
                {result.publishedDate && <div className="text-gray-500">Year: {result.publishedDate}</div>}
              </div>
            </div>
            {result.description && <div className="mb-2"><span className="font-semibold">Description:</span> {result.description}</div>}
            <div className="grid grid-cols-2 gap-2 mt-2">
              {result.language && <div><span className="font-semibold">Languages:</span> {result.language.join(", ")}</div>}
              {result.publisher && <div><span className="font-semibold">Publisher:</span> {result.publisher.join(", ")}</div>}
              {result.isbn && <div><span className="font-semibold">ISBN:</span> {result.isbn.join(", ")}</div>}
              {result.pageCount && <div><span className="font-semibold">Pages:</span> {result.pageCount}</div>}
            </div>
          </div>
        )}
        {result.resultType === "exercise" && (
          <div>
            <div className="flex gap-4 mb-4">
              {result.gifUrl && (
                <img src={result.gifUrl} alt={result.name} className="w-24 h-24 object-cover rounded-lg border" />
              )}
              <div>
                <h2 className="text-2xl font-bold mb-1">{result.name}</h2>
                {result.bodyParts && <div className="text-gray-600 mb-1">Body part: {result.bodyParts.join(", ")}</div>}
                {result.targetMuscles && <div className="text-gray-500">Target: {result.targetMuscles.join(", ")}</div>}
              </div>
            </div>
            {result.overview && <div className="mb-2"><span className="font-semibold">Overview:</span> {result.overview}</div>}
            {result.instructions && (
              <div className="mb-2">
                <span className="font-semibold">Instructions:</span>
                <ul className="list-disc ml-6 mt-1">
                  {result.instructions.map((step, i) => <li key={i}>{step}</li>)}
                </ul>
              </div>
            )}
            <div className="grid grid-cols-2 gap-2 mt-2">
              {result.secondaryMuscles && <div><span className="font-semibold">Secondary:</span> {result.secondaryMuscles.join(", ")}</div>}
              {result.equipments && <div><span className="font-semibold">Equipment:</span> {result.equipments.join(", ")}</div>}
            </div>
          </div>
        )}
        {result.resultType === "movie" && (
          <div>
            <div className="flex gap-4 mb-4">
              {result.poster && (
                <img src={result.poster} alt={result.title} className="w-24 h-32 object-cover rounded-lg border" />
              )}
              <div>
                <h2 className="text-2xl font-bold mb-1">{result.title}</h2>
                {result.year && <div className="text-gray-600 mb-1">Year: {result.year}</div>}
                {result.mediaType && <div className="text-gray-500">Type: {result.mediaType}</div>}
              </div>
            </div>
            {result.description && <div className="mb-2"><span className="font-semibold">Description:</span> {result.description}</div>}
          </div>
        )}
      </div>
    </div>
  );
}
