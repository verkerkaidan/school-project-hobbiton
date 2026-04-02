"use client";
import { useState } from "react";
import { Modal } from "./Modal";
import { login } from "@/lib/authenticationClient";
import { useRouter } from "next/navigation";
import { LoginForm, ModalProps } from "@/types/interfaces";

export function LoginModal({ isOpen, onClose }: ModalProps) {
    const [form, setForm] = useState<LoginForm>({ email: "", password: "" });
    const [error, setError] = useState<string>("");
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setError("");
    };

    /**
     * Once user pushes login, it first checks if fields are filled, then tries to log user in, if not possible goes to the catch and finally sets loading back to false so user can try again.
     * @param e Event that triggers when submitted
     * @returns null
     */
    const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.email || !form.password) {
        setError("All fields are required.");
        return;
    }
    try {
        setLoading(true);
        await login(form.email, form.password);
        router.push("/");
        router.refresh();
        onClose(); // ✅ only closes on success
    } catch (err) {
        setError(err instanceof Error ? err.message : "Login failed.");
        // modal stays open so the user can retry
    } finally {
        setLoading(false);
    }
};

    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <h2 className="text-xl font-bold mb-4">Login</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                <input
                    name="email"
                    type="email"
                    placeholder="example@gmail.com"
                    value={form.email}
                    onChange={handleChange}
                    className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                />
                <input
                    name="password"
                    type="password"
                    placeholder="Password"
                    value={form.password}
                    onChange={handleChange}
                    className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                />
                <label className="text-sm text-gray-500" htmlFor="password">8 characters, 1 special character, 1 capital letter</label>

                {error && <p className="text-red-500 text-sm">{error}</p>}

                <button
                    type="submit"
                    disabled={loading}
                    className="bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200"
                >
                    {loading ? "Logging in..." : "Login"}
                </button>
                <button type="button" onClick={onClose} className="text-sm text-gray-400 hover:text-gray-600">
                    Cancel
                </button>
            </form>
        </Modal>
    );
}