import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center px-6">
      <div className="glass-card rounded-3xl p-12 max-w-md w-full text-center">
        <p className="text-7xl font-bold text-brand">404</p>
        <h1 className="mt-4 text-2xl font-semibold tracking-tight text-accent">
          Page not found
        </h1>
        <p className="mt-3 text-sm opacity-50">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link
          href="/"
          className="mt-8 inline-block rounded-xl bg-brand px-6 py-3 text-sm font-semibold text-white transition-opacity hover:opacity-90"
        >
          Back to home
        </Link>
      </div>
    </div>
  );
}
