import React, {useState} from "react";
import {motion} from "framer-motion";

export default function SearchBar({
                                      onSearch,
                                      queryType,
                                      onBack,
                                  }: {
    onSearch: (query: string) => void;
    queryType: string;
    onBack: () => void;
}) {
    const [query, setQuery] = useState("");
    const [error, setError] = useState("");

    // Use React.SyntheticEvent<HTMLFormElement> for the handler type
    const handleSubmit = (e: React.SyntheticEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!query.trim()) {
            setError("Please enter a search term.");
            return;
        }
        setError("");
        onSearch(query);
    };

    return (
        <motion.form
            className="flex flex-col items-center gap-4 mt-12 w-full max-w-md"
            initial={{opacity: 0, y: 30}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
            onSubmit={handleSubmit}
        >
            <button
                type="button"
                className="self-start glass-card glass-card-hover px-6 py-2 rounded-xl shadow-lg text-base font-semibold focus:outline-none focus:ring-2 focus:ring-brand mb-2"
                onClick={onBack}
            >
                ← Back
            </button>
            <label className="text-lg font-semibold mb-2">
                Search for a {queryType.toLowerCase()}:
            </label>
            <input
                className="input input-bordered w-full px-4 py-2 rounded-lg"
                type="text"
                value={query}
                onChange={e => setQuery(e.target.value)}
                placeholder={`Type a ${queryType.toLowerCase()}...`}
            />
            {error && <span className="text-red-500 text-sm">{error}</span>}
            <button
                type="submit"
                className="glass-card glass-card-hover px-6 py-2 rounded-xl shadow-lg text-base font-semibold focus:outline-none focus:ring-2 focus:ring-brand w-full mt-2"
            >
                Search
            </button>
        </motion.form>
    );
}
