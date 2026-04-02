interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface LoginForm {
  email: string;
  password: string;
}

interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
}