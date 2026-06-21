import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import Heading from "@theme/Heading";
import styles from "./index.module.css";

export default function Home(): JSX.Element {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout title="Home" description={siteConfig.tagline}>
      <main className={styles.main}>
        <Heading as="h1">Hobbithon</Heading>
        <p className={styles.lead}>
          Hobbithon is a hobby-sharing social platform built as a polyglot
          microservices system. Users can discover others with shared interests,
          create and react to posts, chat in real-time, and browse a media
          library — all served by a fleet of independently deployable services
          written in Java, TypeScript, C#, Rust, Elixir, and Go.
        </p>
        <p>
          Use the <strong>Docs</strong> link in the navbar to browse the service
          documentation. Each service has its own section in the sidebar
          covering its purpose, API, environment variables, and technology
          stack.
        </p>
        <Link
          className="button button--primary button--lg"
          to="/docs"
        >
          Browse the docs
        </Link>
      </main>
    </Layout>
  );
}
