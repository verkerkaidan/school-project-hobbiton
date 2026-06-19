import { CreateReadingPost } from "@/types/interfaces";

interface ReadingFieldsProps {
    form: CreateReadingPost;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export const ReadingFields = ({ form, onChange }: ReadingFieldsProps) => {
    return (
        <div className="flex flex-col gap-3">
            <div className="grid grid-cols-2 gap-3">
                <div className="flex flex-col gap-1 col-span-2">
                    <label className="text-sm font-medium text-white">Book title</label>
                    <input
                        name="bookTitle"
                        type="text"
                        placeholder="The Pragmatic Programmer"
                        value={form.bookTitle}
                        onChange={onChange}
                        className="border border-none rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand bg-brand/5"
                    />
                </div>
                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-white">Pages read</label>
                    <input
                        name="pagesRead"
                        type="number"
                        min={0}
                        placeholder="25"
                        value={form.pagesRead || ""}
                        onChange={onChange}
                        className="border rounded border-none bg-brand/5 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                    />
                </div>
                <div className="flex flex-col gap-1">
                    <label className="text-sm font-medium text-white">Total pages</label>
                    <input
                        name="totalPagesRead"
                        type="number"
                        min={0}
                        placeholder="100"
                        value={form.totalPagesRead || ""}
                        onChange={onChange}
                        className="border rounded border-none bg-brand/5 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                    />
                </div>
            </div>
            <label className="flex items-center gap-2 text-sm text-white cursor-pointer select-none">
                <input
                    name="bookFinished"
                    type="checkbox"
                    checked={form.bookFinished}
                    onChange={onChange}
                    className="accent-brand w-4 h-4"
                />
                Book finished
            </label>
        </div>
    );
};