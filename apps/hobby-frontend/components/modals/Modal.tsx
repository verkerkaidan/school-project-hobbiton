import React from "react";

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  largeModal?: boolean;
}

export function Modal({
  isOpen,
  onClose,
  children,
  largeModal = false,
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
      <div className={`${largeModal?"w-[60vw]": "w-[30vw]"} bg-background border border-white/10 rounded-xl p-8 mx-4 shadow-2xl max-h-[80vh] overflow-y-auto`}>
        {children}
      </div>
    </div>
  );
}
