"use client";
import { useState } from "react";
import { Modal } from "./Modal";
import { register } from "@/lib/registerClient";
import { useRouter } from "next/navigation";
import { ModalProps, RegisterForm } from "@/types/interfaces";
import { TermsOfServiceModal } from "./TermsOfServiceModal";

type FieldErrors = Partial<Record<keyof RegisterForm, string>>;

const passwordRules = [
  { label: "At least 8 characters", test: (p: string) => p.length >= 8 },
  { label: "One uppercase letter", test: (p: string) => /[A-Z]/.test(p) },
  {
    label: "One special character",
    test: (p: string) => /[^A-Za-z0-9]/.test(p),
  },
];

export function RegisterModal({ isOpen, onClose }: ModalProps) {
  const [form, setForm] = useState<RegisterForm>({
    email: "",
    password: "",
    confirmPassword: "",
    username: "",
    name: "",
  });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [submitError, setSubmitError] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [showPasswordRules, setShowPasswordRules] = useState(false);
  const [showTos, setShowTos] = useState(false);
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: undefined }));
    setSubmitError("");
  };

  const validate = (): boolean => {
    const errors: FieldErrors = {};
    if (!form.username) errors.username = "Username is required.";
    if (!form.email) errors.email = "Email is required.";
    if (!form.password) {
      errors.password = "Password is required.";
    } else {
      const failedRules = passwordRules.filter((r) => !r.test(form.password));
      if (failedRules.length > 0) {
        errors.password = `Password must have: ${failedRules.map((r) => r.label.toLowerCase()).join(", ")}.`;
      }
    }
    if (!form.confirmPassword) {
      errors.confirmPassword = "Please confirm your password.";
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = "Passwords do not match.";
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      setLoading(true);
      await register(form.email, form.password, form.username, form.name);
      // Show TOS modal before completing the flow
      setShowTos(true);
    } catch (err) {
      setSubmitError(
        err instanceof Error
          ? err.message
          : "Registration failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleTosAccept = () => {
    setShowTos(false);
    router.push("/");
    router.refresh();
    onClose();
  };

  return (
    <>
      <Modal isOpen={isOpen && !showTos} onClose={onClose}>
        <h2 className="text-xl font-bold mb-4">Register</h2>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <label
              htmlFor="register-username"
              className="text-sm font-medium text-white"
            >
              Username
            </label>
            <input
              id="register-username"
              name="username"
              type="text"
              placeholder="e.g. johndoe"
              value={form.username}
              onChange={handleChange}
              className={`border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand ${fieldErrors.username ? "border-red-400" : ""}`}
            />
            {fieldErrors.username && (
              <p className="text-red-500 text-xs">{fieldErrors.username}</p>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label
              htmlFor="register-email"
              className="text-sm font-medium text-white"
            >
              Email
            </label>
            <input
              id="register-email"
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
              htmlFor="register-password"
              className="text-sm font-medium text-white"
            >
              Password
            </label>
            <input
              id="register-password"
              name="password"
              type="password"
              placeholder="Password"
              value={form.password}
              onChange={handleChange}
              onFocus={() => setShowPasswordRules(true)}
              className={`border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand ${fieldErrors.password ? "border-red-400" : ""}`}
            />
            {showPasswordRules && (
              <ul className="text-xs mt-1 flex flex-col gap-0.5">
                {passwordRules.map((rule) => (
                  <li
                    key={rule.label}
                    className={
                      rule.test(form.password)
                        ? "text-green-600"
                        : "text-gray-400"
                    }
                  >
                    {rule.test(form.password) ? "✓" : "○"} {rule.label}
                  </li>
                ))}
              </ul>
            )}
            {fieldErrors.password && (
              <p className="text-red-500 text-xs">{fieldErrors.password}</p>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label
              htmlFor="register-confirm-password"
              className="text-sm font-medium text-white"
            >
              Confirm password
            </label>
            <input
              id="register-confirm-password"
              name="confirmPassword"
              type="password"
              placeholder="Confirm password"
              value={form.confirmPassword}
              onChange={handleChange}
              className={`border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand ${fieldErrors.confirmPassword ? "border-red-400" : ""}`}
            />
            {fieldErrors.confirmPassword && (
              <p className="text-red-500 text-xs">
                {fieldErrors.confirmPassword}
              </p>
            )}
          </div>

        {submitError && (
          <div className="text-sm bg-red-200 border border-red-300 rounded px-3 py-2 text-red-600">
            {submitError}
          </div>
        )}

          <button
            type="submit"
            disabled={loading}
            className="bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200 disabled:opacity-60"
          >
            {loading ? "Registering..." : "Register"}
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

      <TermsOfServiceModal isOpen={showTos} onAccept={handleTosAccept} />
    </>
  );
}
