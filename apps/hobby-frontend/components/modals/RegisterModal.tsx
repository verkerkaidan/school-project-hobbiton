"use client";
import { useState } from "react";
import { Modal } from "./Modal";
import { register } from "@/lib/authenticationClient";
import { useRouter } from "next/navigation";

export function RegisterModal({ isOpen, onClose }: ModalProps) {
    const [form, setForm] = useState<RegisterForm>({
        email: "",
        password: "",
        confirmPassword: "",
    });
    const [error, setError] = useState<string>("");
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setError("");
    };

    /**
     * Once user pushes register, it first checks if fields are filled, then tries register user, if not possible goes to the catch and finally sets loading back to false so user can try again.
     * @param e Event that triggers when submitted
     * @returns 
     */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!form.email || !form.password || !form.confirmPassword) {
            setError("All fields are required.");
            return;
        }
        if (form.password !== form.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        if (form.password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        try {
            setLoading(true);
            await register(form.email, form.password);
            router.push("/");
            router.refresh();
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Registration failed.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <h2 className="text-xl font-bold mb-4">Register</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                <input
                    name="email"
                    type="email"
                    placeholder="Email"
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
                <input
                    name="confirmPassword"
                    type="password"
                    placeholder="Confirm Password"
                    value={form.confirmPassword}
                    onChange={handleChange}
                    className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
                />

                {error && <p className="text-red-500 text-sm">{error}</p>}

                <button
                    type="submit"
                    disabled={loading}
                    className="bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200"
                >
                    {loading ? "Registering..." : "Register"}
                </button>
                <button type="button" onClick={onClose} className="text-sm text-gray-400 hover:text-gray-600">
                    Cancel
                </button>
            </form>
        </Modal>
    );
}