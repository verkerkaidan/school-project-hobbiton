"use client";
import { useState } from "react";
import { Modal } from "./Modal";
import { login } from "@/lib/authenticationClient";
import { useRouter } from "next/navigation";
import { LoginForm, ModalProps } from "@/types/interfaces";

export function LoginModal({ isOpen, onClose }: ModalProps) {
  const [form, setForm] = useState<LoginForm>({ email: "", password: "" });
  const [fieldErrors, setFieldErrors] = useState<Partial<LoginForm>>({});
  const [submitError, setSubmitError] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
    setSubmitError("");
  };

  const validate = (): boolean => {
    const errors: Partial<LoginForm> = {};
    if (!form.email) errors.email = "Email is required.";
    if (!form.password) errors.password = "Password is required.";
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      setLoading(true);
      await login(form.email, form.password);
      router.push("/");
      router.refresh();
      onClose();
    } catch (err) {
      setSubmitError(
        err instanceof Error
          ? err.message
          : "Incorrect email or password. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <h2 className="text-xl font-bold mb-4">Login</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="flex flex-col gap-1">
          <label
            htmlFor="login-email"
            className="text-sm font-medium text-white"
          >
            Email
          </label>
          <input
            id="login-email"
            name="email"
            type="email"
            placeholder="you@example.com"
            value={form.email}
            onChange={handleChange}
            className={`border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand ${fieldErrors.email ? "border-red-400" : ""}`}
          />
          {fieldErrors.email && (
            <p className="text-red-500 text-xs">{fieldErrors.email}</p>
          )}
        </div>

        <div className="flex flex-col gap-1">
          <label
            htmlFor="login-password"
            className="text-sm font-medium text-white"
          >
            Password
          </label>
          <input
            id="login-password"
            name="password"
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            className={`border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand ${fieldErrors.password ? "border-red-400" : ""}`}
          />
          {fieldErrors.password && (
            <p className="text-red-500 text-xs">{fieldErrors.password}</p>
          )}
        </div>

        {submitError && (
          <p className="text-red-500 text-sm bg-red-50 border border-red-200 rounded px-3 py-2">
            {submitError}
          </p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200 disabled:opacity-60"
        >
          {loading ? "Logging in..." : "Login"}
        </button>
        <button
          type="button"
          onClick={onClose}
          className="text-sm text-gray-400 hover:text-gray-600"
        >
          Cancel
        </button>
      </form>
    </Modal>
  );
}
