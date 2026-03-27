import {Sidebar} from '@/components/layout/Sidebar';
import {Feed} from '@/components/feed/Feed';
import {feedItems} from '@/data/mockPosts';

export default function HomePage() {
    return (
        <div className="flex min-h-screen">
            <Sidebar/>

            <main className="flex-1 ml-20 md:ml-64 max-w-5xl mx-auto px-6 pt-16 pb-20">
                <header className="mb-20 space-y-4">
                    <h1 className="text-5xl md:text-7xl font-serif italic text-white">
                        Welcome <span className="text-brand">Demiiee</span>
                    </h1>
                    <p className="text-accent text-lg max-w-sm">
                        Jouw persoonlijke stroom van literatuur, cinema en beweging.
                    </p>
                </header>
                <Feed posts={feedItems}/>
            </main>
        </div>
    );
}