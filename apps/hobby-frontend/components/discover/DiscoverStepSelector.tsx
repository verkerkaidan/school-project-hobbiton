import { motion } from "framer-motion";

const HOBBIES = [
  { label: "Book", value: "BOOK", icon: "📚" },
  { label: "Exercise", value: "EXERCISE", icon: "🏋️" },
  { label: "Movie", value: "MOVIE", icon: "🎬" },
];

export default function DiscoverStepSelector({ onSelect }: { onSelect: (type: string) => void }) {
  return (
    <div className="flex flex-col items-center gap-8 mt-12">
      <motion.h2
        className="text-3xl font-bold mb-6"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        What do you want to discover?
      </motion.h2>
      <div className="flex gap-8">
        {HOBBIES.map((hobby, idx) => (
          <motion.button
            key={hobby.value}
            className="glass-card glass-card-hover px-8 py-6 rounded-2xl flex flex-col items-center shadow-lg text-lg font-semibold focus:outline-none focus:ring-2 focus:ring-brand"
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 + idx * 0.3, duration: 0.6 }}
            whileHover={{ scale: 1.08 }}
            whileTap={{ scale: 0.97 }}
            onClick={() => onSelect(hobby.value)}
            aria-label={hobby.label}
          >
            <span className="text-4xl mb-2">{hobby.icon}</span>
            {hobby.label}
          </motion.button>
        ))}
      </div>
    </div>
  );
}

