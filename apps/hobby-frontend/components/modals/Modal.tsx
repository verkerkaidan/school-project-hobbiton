import React from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
}

export function Modal({
  isOpen,
  onClose,
  children,
}: ModalProps & { children: React.ReactNode }) {
  const mouseDownTarget = React.useRef<EventTarget | null>(null);

  if (!isOpen) return null;
  return (
    <div
      className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50"
      onMouseDown={(e) => {
        mouseDownTarget.current = e.target;
      }}
      onClick={(e) => {
        if (mouseDownTarget.current === e.currentTarget) onClose();
      }}
    >
      <div className="bg-zinc-900 border border-white/10 rounded-xl p-8 max-w-md w-full mx-4 shadow-2xl max-h-[80vh] overflow-y-auto">
        {children}
      </div>
    </div>
  );
}
