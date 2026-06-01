"use client";
import {useState} from "react";
import DiscoverStepSelector from "@/components/discover/DiscoverStepSelector";
import SearchBar from "@/components/discover/SearchBar";
import ResultsList from "@/components/discover/ResultsList";
import ResultDetailModal from "@/components/discover/ResultDetailModal";
import Loading from "../loading";

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

export default function DiscoverPage() {
    const [step, setStep] = useState<"select" | "search" | "results">("select");
    const [queryType, setQueryType] = useState<string>("");
    const [results, setResults] = useState<ResultItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [selectedResult, setSelectedResult] = useState<ResultItem | null>(null);

    const handleSelect = (type: string) => {
        setQueryType(type);
        setStep("search");
        setResults([]);
        setError("");
    };

    const handleSearch = async (query: string) => {
        setLoading(true);
        setError("");
        setResults([]);
        try {
            // Adjust port if needed; default Spring Boot is 8080
            const res = await fetch(
                `${process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE}/api/library/search`,
                {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({type: queryType, query, maxResults: 10}),
                }
            );
            if (!res.ok) throw new Error("Failed to fetch results");
            const data = await res.json();
            if (!data.success) throw new Error(data.message || "Unknown error");
            setResults(data.data || []);
            setStep("results");
        } catch (e: unknown) {
            if (e instanceof Error) setError(e.message);
            else setError("Error searching");
        } finally {
            setLoading(false);
        }
    };

    const handleBack = () => {
        if (step === "search") {
            setStep("select");
            setQueryType("");
        } else if (step === "results") {
            setStep("search");
        }
        setError("");
        setResults([]);
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-[70vh] w-full px-2">
            {loading && <Loading/>}
            {!loading && step === "select" && <DiscoverStepSelector onSelect={handleSelect}/>}
            {!loading && step === "search" && (
                <SearchBar onSearch={handleSearch} queryType={queryType} onBack={handleBack}/>
            )}
            {!loading && step === "results" && (
                <ResultsList
                    results={results}
                    loading={loading}
                    error={error}
                    onBack={handleBack}
                    onSelect={setSelectedResult}
                />
            )}
            {selectedResult && (
                <ResultDetailModal result={selectedResult} onClose={() => setSelectedResult(null)}/>
            )}
        </div>
    );
}
