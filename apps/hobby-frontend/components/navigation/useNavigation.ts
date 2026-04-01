import {usePathname, useRouter} from 'next/navigation';

export function useNavigation() {
  const pathname = usePathname();
  const router = useRouter();

  function isActive(href: string) {
    return pathname === href;
  }

  function navigate(href: string) {
    if (pathname !== href) {
      router.push(href);
    }
  }

  return { pathname, isActive, navigate };
}

