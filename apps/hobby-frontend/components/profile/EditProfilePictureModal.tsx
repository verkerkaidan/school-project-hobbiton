"use client";

import React, { useRef, useState, useEffect } from "react";
import { createPortal } from "react-dom";
import { motion, AnimatePresence } from "framer-motion";
import Image from "next/image";

interface EditProfilePictureModalProps {
  isOpen: boolean;
  currentImage: string;
  username: string;
  onClose: () => void;
  onSave: (file: File) => Promise<void>;
}

export function EditProfilePictureModal({
  isOpen,
  currentImage,
  username,
  onClose,
  onSave,
}: EditProfilePictureModalProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [saving, setSaving] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const handleFileChange = (file: File) => {
    if (!file.type.startsWith("image/")) return;
    setSelectedFile(file);
    setPreview(URL.createObjectURL(file));
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleFileChange(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files?.[0];
    if (file) handleFileChange(file);
  };

  const handleSave = async () => {
    if (!selectedFile) return;
    setSaving(true);
    try {
      onSave(selectedFile);
      handleClose();
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    setPreview(null);
    setSelectedFile(null);
    setSaving(false);
    if (fileInputRef.current) fileInputRef.current.value = "";
    onClose();
  };

  if (!mounted) return null;

  return createPortal(
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          onClick={(e) => e.target === e.currentTarget && handleClose()}
        >
          {/* Backdrop */}
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" />

          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 12 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 12 }}
            transition={{ duration: 0.2 }}
            className="relative w-full max-w-sm bg-[#0f0f1a] border border-white/10 rounded-3xl shadow-2xl p-8 flex flex-col gap-6"
          >
            {/* Header */}
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-white">
                Edit Profile Picture
              </h2>
              <button
                onClick={handleClose}
                className="p-1.5 rounded-xl text-accent hover:text-white hover:bg-white/10 transition-colors"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>

            {/* Preview */}
            <div className="flex justify-center">
              <div className="relative w-36 h-36 rounded-full overflow-hidden border-2 border-brand/30 shadow-lg">
                <Image
                  src={preview ?? currentImage}
                  alt={username}
                  fill
                  className="object-cover"
                />
              </div>
            </div>

            {/* Drop zone */}
            <button
              type="button"
              onDragOver={(e) => {
                e.preventDefault();
                setDragOver(true);
              }}
              onDragLeave={() => setDragOver(false)}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`w-full flex flex-col items-center gap-2 py-6 rounded-2xl border-2 border-dashed transition-colors cursor-pointer
                ${
                  dragOver
                    ? "border-brand/60 bg-brand/10"
                    : "border-white/15 hover:border-brand/40 hover:bg-white/5"
                }`}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="text-accent"
              >
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <span className="text-sm text-accent">
                {selectedFile
                  ? selectedFile.name
                  : "Click or drag & drop to upload"}
              </span>
              <span className="text-xs text-white/30">PNG, JPG, WEBP</span>
            </button>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/png,image/jpeg,image/webp"
              className="hidden"
              onChange={handleInputChange}
            />

            {/* Actions */}
            <div className="flex gap-3">
              <button
                onClick={handleClose}
                className="flex-1 py-2.5 rounded-xl border border-white/10 text-sm text-accent hover:text-white hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <motion.button
                whileHover={selectedFile && !saving ? { scale: 1.02 } : {}}
                whileTap={selectedFile && !saving ? { scale: 0.98 } : {}}
                onClick={handleSave}
                disabled={!selectedFile || saving}
                className="flex-1 py-2.5 rounded-xl bg-brand/20 border border-brand/30 text-sm text-brand font-semibold
                           hover:bg-brand/30 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              >
                {saving ? "Saving..." : "Save"}
              </motion.button>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body,
  );
}
