import {motion} from "framer-motion";

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
    resultType: "exercise";
};
type MovieResult = {
    movieId: string;
    title: string;
    description?: string;
    poster?: string;
    year?: string;
    resultType: "movie";
};
type ResultItem = BookResult | ExerciseResult | MovieResult;

export default function ResultsList({
                                        results,
                                        loading,
                                        error,
                                        onBack,
                                        onSelect,
                                    }: {
    results: ResultItem[];
    loading: boolean;
    error: string;
    onBack: () => void;
    onSelect?: (result: ResultItem) => void;
}) {
    return (
        <motion.div
            className="flex flex-col items-center mt-12 w-full max-w-2xl"
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
        >
            <button
                type="button"
                className="self-start text-brand underline text-sm mb-4"
                onClick={onBack}
            >
                ← Back
            </button>
            <h3 className="text-2xl font-bold mb-4">Results</h3>
            {loading && <div className="text-accent">Loading...</div>}
            {error && <div className="text-red-500">{error}</div>}
            {!loading && !error && results.length === 0 && (
                <div className="text-accent">No results found.</div>
            )}
            <ul className="w-full">
                {results.map((item, idx) => {
                    if (item.resultType === "book") {
                        return (
                            <li
                                key={item.title + idx}
                                className="glass-card px-6 py-4 rounded-xl mb-4 shadow-md flex gap-4 items-center cursor-pointer hover:border-[2px] hover:border-brand-muted hover:shadow-pink-100 transition"
                                onClick={() => onSelect && onSelect(item)}
                            >
                                {item.thumbnailUrl ? (
                                    <img
                                        src={item.thumbnailUrl}
                                        alt={item.title}
                                        className="w-16 h-20 object-cover rounded-md border border-gray-200 bg-white"
                                    />
                                ) : (
                                    <div
                                        className="w-16 h-20 bg-gray-200 rounded-md flex items-center justify-center text-gray-400">?</div>
                                )}
                                <div>
                                    <div className="font-semibold text-lg">{item.title}</div>
                                    {item.authors &&
                                        <div className="text-sm text-gray-500">{item.authors.join(", ")}</div>}
                                    {item.description &&
                                        <div className="text-sm text-gray-500 mt-1">{item.description}</div>}
                                </div>
                            </li>
                        );
                    } else if (item.resultType === "exercise") {
                        return (
                            <li
                                key={item.exerciseId}
                                className="glass-card px-6 py-4 rounded-xl mb-4 shadow-md flex gap-4 items-center cursor-pointer hover:border-[2px] hover:border-brand-muted hover:shadow-pink-100 transition"
                                onClick={() => onSelect && onSelect(item)}
                            >
                                {item.gifUrl ? (
                                    <img
                                        src={item.gifUrl}
                                        alt={item.name}
                                        className="w-16 h-16 object-cover rounded-md border border-gray-200 bg-white"
                                    />
                                ) : (
                                    <div
                                        className="w-16 h-16 bg-gray-200 rounded-md flex items-center justify-center text-gray-400">?</div>
                                )}
                                <div>
                                    <div className="font-semibold text-lg">{item.name}</div>
                                    {item.overview && <div className="text-sm text-gray-500 mt-1">{item.overview}</div>}
                                </div>
                            </li>
                        );
                    } else if (item.resultType === "movie") {
                        return (
                            <li
                                key={item.movieId}
                                className="glass-card px-6 py-4 rounded-xl mb-4 shadow-md flex gap-4 items-center cursor-pointer hover:border-[2px] hover:border-brand-muted hover:shadow-pink-100 transition"
                                onClick={() => onSelect && onSelect(item)}
                            >
                                {item.poster ? (
                                    <img
                                        src={item.poster}
                                        alt={item.title}
                                        className="w-16 h-20 object-cover rounded-md border border-gray-200 bg-white"
                                    />
                                ) : (
                                    <div
                                        className="w-16 h-20 bg-gray-200 rounded-md flex items-center justify-center text-gray-400">?</div>
                                )}
                                <div>
                                    <div className="font-semibold text-lg">{item.title}</div>
                                    {item.description &&
                                        <div className="text-sm text-gray-500 mt-1">{item.description}</div>}
                                </div>
                            </li>
                        );
                    } else {
                        return null;
                    }
                })}
            </ul>
        </motion.div>
    );
}
