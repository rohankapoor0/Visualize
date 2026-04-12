import { ArrowRight, BarChart2, Lightbulb, Zap, Database } from 'lucide-react';
import { NavLink } from 'react-router-dom';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-50 font-sans">
      <header className="fixed top-0 inset-x-0 bg-white/80 backdrop-blur-md z-50 border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-xl">I</div>
            <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-purple-600">Visualize AI</span>
          </div>
          <nav className="flex items-center gap-6">
            <a href="#features" className="text-sm font-medium text-slate-600 hover:text-indigo-600 transition-colors">Features</a>
            <NavLink to="/app/upload" className="bg-indigo-600 text-white px-5 py-2.5 rounded-full text-sm font-medium hover:bg-indigo-700 transition-colors shadow-sm">
              Get Started
            </NavLink>
          </nav>
        </div>
      </header>

      <main>
        {/* Hero Section */}
        <section className="pt-32 pb-20 px-6 text-center max-w-5xl mx-auto">
          <h1 className="text-5xl md:text-7xl font-extrabold text-slate-900 tracking-tight leading-tight mb-6">
            Your Data. One Place. <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">Actually Understood.</span>
          </h1>
          <p className="text-xl md:text-2xl text-slate-600 mb-10 max-w-3xl mx-auto leading-relaxed">
            Visualize AI brings your datasets, charts, trends, and actionable insights into one clean workspace so nothing slips through.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <NavLink to="/app/upload" className="flex items-center justify-center gap-2 w-full sm:w-auto bg-indigo-600 text-white px-8 py-4 rounded-full text-lg font-medium hover:bg-indigo-700 transition-all shadow-lg hover:shadow-indigo-500/25">
              Upload Dataset
              <ArrowRight size={20} />
            </NavLink>
          </div>
          
          <div className="mt-16 relative mx-auto bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200/50">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center gap-2 bg-slate-50">
              <div className="w-3 h-3 rounded-full bg-red-400"></div>
              <div className="w-3 h-3 rounded-full bg-amber-400"></div>
              <div className="w-3 h-3 rounded-full bg-emerald-400"></div>
            </div>
            {/* Dashboard Mockup Image / Placeholder */}
            <div className="aspect-[16/9] bg-slate-100 flex items-center justify-center relative overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-br from-indigo-50 to-purple-50 opacity-50"></div>
                <BarChart2 size={120} className="text-indigo-200" />
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section id="features" className="py-24 bg-white px-6">
          <div className="max-w-7xl mx-auto text-center">
            <h2 className="text-3xl font-bold text-slate-900 mb-16">Everything you need. Nothing you don't.</h2>
            
            <div className="grid md:grid-cols-3 gap-8 text-left">
              <div className="p-8 rounded-3xl bg-slate-50 border border-slate-100 hover:shadow-lg transition-shadow">
                <div className="w-12 h-12 rounded-2xl bg-indigo-100 flex items-center justify-center text-indigo-600 mb-6">
                  <Database size={24} />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">Instant Processing</h3>
                <p className="text-slate-600">Upload CSV or XLSX files and Visualize AI automatically structures and types your columns perfectly.</p>
              </div>
              
              <div className="p-8 rounded-3xl bg-slate-50 border border-slate-100 hover:shadow-lg transition-shadow">
                <div className="w-12 h-12 rounded-2xl bg-purple-100 flex items-center justify-center text-purple-600 mb-6">
                  <BarChart2 size={24} />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">Auto Visualization</h3>
                <p className="text-slate-600">Dynamic generation of bar, pie, line, and scatter charts directly tailored to your unique data types.</p>
              </div>
              
              <div className="p-8 rounded-3xl bg-slate-50 border border-slate-100 hover:shadow-lg transition-shadow">
                <div className="w-12 h-12 rounded-2xl bg-amber-100 flex items-center justify-center text-amber-600 mb-6">
                  <Lightbulb size={24} />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">AI Deep Insights</h3>
                <p className="text-slate-600">We analyze trends, detect anomalies, and present actionable insights in simple, non-technical language.</p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="bg-slate-900 py-12 px-6 text-center text-slate-400">
        <p>© 2026 Visualize AI. Built for modern data analysis.</p>
      </footer>
    </div>
  );
}
