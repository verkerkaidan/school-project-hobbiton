"use client";
import {useState} from "react";
import DiscoverStepSelector from "@/components/discover/DiscoverStepSelector";
import SearchBar from "@/components/discover/SearchBar";
import ResultsList from "@/components/discover/ResultsList";
import ResultDetailModal from "@/components/discover/ResultDetailModal";
import Loading from "../loading";
import {searchLibrary} from "@/lib/libraryClient";
import {useRouter} from "next/navigation";
import {User} from "@/types/profile";
import {useErrorToast} from "@/components/ui/ErrorToastProvider";

type BookResult = {
    id?: string;
    title: string;
    authors?: string[];
    publishers?: string[];
    publishedDate?: string;
    isbn?: string[];
    pageCount?: number;
    languages?: string[];
    coverId?: string;
    thumbnailUrl?: string;
    resultType: "book";
};
type ExerciseResult = {
    exerciseId: string;
    name: string;
    instructions?: string[];
    gifUrl?: string;
    bodyParts?: string[];
    targetMuscles?: string[];
    secondaryMuscles?: string[];
    equipments?: string[];
    resultType: "exercise";
};
type MovieResult = {
    id: string;
    title: string;
    year?: string;
    rated?: string;
    released?: string;
    runtime?: string;
    genre?: string;
    director?: string;
    writer?: string;
    actors?: string;
    plot?: string;
    language?: string;
    country?: string;
    awards?: string;
    poster?: string;
    imdbRating?: string;
    mediaType: string;
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
    const router = useRouter();
    const {showErrorToast} = useErrorToast();

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
            const data = await searchLibrary(queryType, query);

            if (
                queryType == "USER"
                && data.data
            ) {
                const user = data.data as User;
                console.log(`new route: /profile/${user.username}`)
                router.push(`/profile/${user.username}`);
                return;
            }
            setResults(data.data! || []);
            setStep("results");
        } catch (e: unknown) {
            if (e instanceof Error) setError(e.message);
            else setError("Error searching");
            showErrorToast("Unfortunately", "We couldn't find what you were looking for", "error");
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
                    onBackAction={handleBack}
                    onSelectAction={setSelectedResult}
                />
            )}
            {selectedResult && (
                <ResultDetailModal result={selectedResult} onClose={() => setSelectedResult(null)}/>
            )}
        </div>
    );
}
