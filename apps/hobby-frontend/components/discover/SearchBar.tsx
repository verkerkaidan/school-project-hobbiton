"use client"

import React, { useState } from "react";
import { motion } from "framer-motion";

export default function SearchBar({
                                      onSearch,
                                      queryType,
                                      onBack,
                                      showBackButton = true, // Nieuwe prop met default op true
                                  }: {
    onSearch: (query: string) => void;
    queryType: string;
    onBack?: () => void; // Nu optioneel gemaakt
    showBackButton?: boolean;
}) {
    const [query, setQuery] = useState("");
    const [error, setError] = useState("");

    const handleSearch = () => {
        if (!query.trim()) {
            setError("Please enter a search term.");
            return;
        }
        setError("");
        onSearch(query);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter") {
            e.preventDefault();
            e.stopPropagation();
            handleSearch();
        }
    };

    return (
        <motion.div
            className="flex flex-col items-center gap-4 mt-4 w-full" /* Layout compacter gemaakt voor de modal grid */
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
        >
            {/* Alleen renderen als showBackButton true is */}
            {showBackButton && (
                <button
                    type="button"
                    className="self-start glass-card glass-card-hover px-6 py-2 rounded-xl shadow-lg text-base font-semibold focus:outline-none focus:ring-2 focus:ring-brand mb-2"
                    onClick={onBack}
                >
                    ← Back
                </button>
            )}

            <label className="text-sm font-semibold self-start opacity-80">
                Search for a {queryType.toLowerCase()}:
            </label>

            <div className="flex gap-2 w-full">
                <input
                    className="np-input" /* Gebruik de stijlen uit je global.css */
                    type="text"
                    value={query}
                    onChange={e => setQuery(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder={`Type a ${queryType.toLowerCase()}...`}
                />
                <button
                    type="button"
                    onClick={handleSearch}
                    className="np-submit-btn" /* Consistent met de hoofdknop */
                >
                    Search
                </button>
            </div>
            {error && <span className="text-red-500 text-sm self-start">{error}</span>}
        </motion.div>
    );
}