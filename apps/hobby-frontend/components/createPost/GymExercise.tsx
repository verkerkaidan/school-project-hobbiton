import { Exercise } from "@/types/posts";

interface GymExerciseProps {
    ex: Exercise;
    index: number;
    onRemove: (i: number) => void;
    onChange: (i: number, field: keyof Exercise, value: string) => void;
}

export const GymExercise = ({ ex, index, onRemove, onChange }: GymExerciseProps) => {
    return (
        <div className="border rounded-xl p-3 flex flex-col gap-2 bg-background border-2 border-brand">
            <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-600">Exercise {index + 1}</span>
                <button
                    type="button"
                    onClick={() => onRemove(index)}
                    className="text-xs text-red-400 hover:text-red-600"
                >
                    Remove
                </button>
            </div>
            <input
                type="text"
                placeholder="Exercise name"
                value={ex.title}
                onChange={(e) => onChange(index, "title", e.target.value)}
                className="text-black border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
            />
            <div className="grid grid-cols-3 gap-2">
                <div className="flex flex-col gap-1">
                    <label className="text-xs text-gray-500">Reps</label>
                    <input
                        type="number"
                        min={0}
                        placeholder="12"
                        value={ex.reps || ""}
                        onChange={(e) => onChange(index, "reps", e.target.value)}
                        className="text-black border rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                    />
                </div>
                <div className="flex flex-col gap-1">
                    <label className="text-xs text-gray-500">Weight (kg)</label>
                    <input
                        type="number"
                        min={0}
                        step={0.25}
                        placeholder="40"
                        value={ex.weight || ""}
                        onChange={(e) => onChange(index, "weight", e.target.value)}
                        className="text-black border rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                    />
                </div>
                <div className="flex flex-col gap-1">
                    <label className="text-xs text-gray-500">RPE (1–10)</label>
                    <input
                        type="number"
                        min={1}
                        max={10}
                        placeholder="7"
                        value={ex.rpe || ""}
                        onChange={(e) => onChange(index, "rpe", e.target.value)}
                        className="text-black border rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                    />
                </div>
            </div>
        </div>
    );
};