import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import graphScreenImg from '../assets/graph_screen.png';
import updatedgraphImg from '../assets/updatedgraph_screen.png';

export default function LandingPage() {
  const { currentUser, userData } = useAuth();
  const [activePopup, setActivePopup] = useState(null); // 'email' or 'privacy'

  return (
    <div className="bg-surface text-on-surface min-h-screen font-body selection:bg-primary-fixed selection:text-primary relative">
      {/* Visual Background Layer */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute inset-0 bg-grid-pattern opacity-[0.2]"></div>
        <div className="absolute inset-0 mesh-gradient-1"></div>
        <div className="absolute inset-0 mesh-gradient-2"></div>
        <div className="absolute inset-0 mesh-gradient-3"></div>
        {/* Background Decorative Elements */}
        <div className="absolute top-[5%] -left-20 w-[500px] h-[500px] bg-primary/10 floating-blur"></div>
        <div className="absolute top-[40%] -right-40 w-[600px] h-[600px] bg-indigo-600/5 floating-blur"></div>
        <div className="absolute bottom-[10%] left-[10%] w-[400px] h-[400px] bg-tertiary-fixed/10 floating-blur"></div>
        {/* Abstract Geometric Shapes */}
        <div className="absolute top-[20%] right-[10%] w-[500px] h-[500px] soft-outline rotate-[45deg] animate-float opacity-50"></div>
        <div className="absolute bottom-[30%] left-[5%] w-[350px] h-[350px] soft-outline -rotate-[20deg] animate-float opacity-30" style={{ animationDelay: '-2s' }}></div>
        <div className="absolute top-[60%] left-[45%] w-[450px] h-[450px] border border-primary/5 rounded-full opacity-40"></div>
        {/* Floating UI Background Decorations */}
        <div className="absolute top-[15%] left-[5%] p-4 bg-white/20 backdrop-blur-xl rounded-lg border border-white/30 shadow-sm rotate-12 opacity-20 hidden lg:block">
          <div className="w-24 h-2 bg-primary/20 rounded mb-2"></div>
          <div className="w-16 h-2 bg-primary/10 rounded"></div>
        </div>
        <div className="absolute bottom-[40%] right-[3%] p-4 bg-white/20 backdrop-blur-xl rounded-lg border border-white/30 shadow-sm -rotate-12 opacity-20 hidden lg:block">
          <div className="flex gap-1">
            <div className="w-2 h-8 bg-tertiary/20 rounded-t"></div>
            <div className="w-2 h-12 bg-tertiary/30 rounded-t"></div>
            <div className="w-2 h-6 bg-tertiary/20 rounded-t"></div>
          </div>
        </div>
      </div>

      {/* Top Navigation Shell */}
      <nav className="docked full-width top-0 sticky z-50 glass-nav flex justify-between items-center h-20 px-6 lg:px-12 transition-all duration-300 border-b border-indigo-600/5">
        <div className="flex items-center gap-2">
          <span className="text-xl font-extrabold tracking-tighter text-on-surface font-headline">Visualize AI</span>
        </div>
        <div className="hidden md:flex items-center gap-10">
          <a className="text-primary font-semibold font-body text-sm transition-colors" href="#">Overview</a>
        </div>
        <div className="flex items-center gap-4">
          {currentUser ? (
            <div className="flex items-center gap-3">
              <div className="text-right hidden md:block">
                <p className="text-sm font-bold font-headline text-slate-900 leading-none">{userData?.name || 'User'}</p>
                <p className="text-xs text-slate-500 font-medium mt-0.5">{userData?.plan || 'Free Plan'}</p>
              </div>
              <div className="w-10 h-10 rounded-full border-2 border-primary/10 bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold shadow-sm">
                {userData?.name?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U'}
              </div>
            </div>
          ) : (
            <NavLink 
              to="/login"
              className="px-6 py-2.5 rounded-full signature-gradient text-white text-sm font-bold tracking-tight shadow-lg shadow-indigo-500/20 active:scale-95 transition-transform"
            >
              Login
            </NavLink>
          )}
        </div>
      </nav>

      <main className="relative z-10 overflow-hidden">
        {/* Hero Section */}
        <section className="relative pt-24 pb-32 px-6 lg:px-12 max-w-7xl mx-auto overflow-visible">
          <div className="grid lg:grid-cols-12 gap-16 items-center">
            <div className="lg:col-span-6 z-10">
              <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-tertiary-fixed text-on-tertiary-fixed-variant text-xs font-bold mb-8 uppercase tracking-widest ai-chip-glow">
                <span className="material-symbols-outlined text-sm" data-icon="auto_awesome">auto_awesome</span>
                Intelligence Reimagined
              </div>
              <h1 className="text-5xl lg:text-7xl font-extrabold font-headline leading-[1.05] tracking-tighter text-on-surface mb-8">
                Your Data. <br/>One Place. <br/><span className="text-primary">Actually Understood.</span>
              </h1>
              <p className="text-lg lg:text-xl text-on-surface-variant font-body leading-relaxed mb-12 max-w-lg">
                Visualize AI brings your datasets, charts, trends, and actionable insights into one clean workspace so nothing slips through.
              </p>
              <div className="flex flex-col sm:flex-row gap-4">
                <NavLink 
                  to={currentUser ? "/app/upload" : "/signup"}
                  className="px-10 py-5 rounded-xl signature-gradient text-white font-bold text-lg shadow-xl shadow-indigo-500/30 hover:scale-[1.02] active:scale-95 transition-all text-center"
                >
                  Get Started
                </NavLink>
              </div>
            </div>
            {/* Dashboard Preview Hero Image */}
            <div className="lg:col-span-6 relative hidden md:block">
              <div className="relative z-10 p-4 bg-white/40 backdrop-blur-md rounded-xl dashboard-preview-shadow border border-slate-200/50 dashboard-preview-shadow">
                <img alt="Visualize AI Dashboard Interface" className="rounded-lg w-full object-cover aspect-video shadow-2xl" src={graphScreenImg} />
                {/* Floating Insight Card */}
                <div className="absolute -bottom-10 -left-10 p-6 bg-surface-container-lowest rounded-xl shadow-2xl max-w-[240px] animate-float border border-white/50">
                  <div className="flex items-center gap-3 mb-4">
                    <div className="w-10 h-10 rounded-full bg-tertiary-fixed flex items-center justify-center">
                      <span className="material-symbols-outlined text-on-tertiary-fixed-variant" data-icon="trending_up">trending_up</span>
                    </div>
                    <div className="text-sm font-bold font-headline text-on-surface">Growth Insight</div>
                  </div>
                  <p className="text-xs text-on-surface-variant leading-relaxed mb-4">AI detected a 24% increase in customer retention for the last quarter based on menu changes.</p>
                  <div className="h-1.5 w-full bg-surface-container rounded-full overflow-hidden">
                    <div className="h-full bg-tertiary-container w-[75%]"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Trusted By Section */}
        <section className="py-16 px-6 border-y border-slate-200/50 bg-white/30 backdrop-blur-sm relative overflow-hidden">
          <div className="max-w-7xl mx-auto flex flex-col items-center">
            <p className="text-[10px] uppercase tracking-[0.3em] font-bold text-slate-400 mb-10">Trusted by modern industry leaders</p>
            <div className="flex flex-wrap justify-center items-center gap-12 md:gap-20 opacity-40 grayscale">
              <div className="flex items-center gap-2 font-headline font-extrabold text-xl font-bold">
                <span className="material-symbols-outlined text-2xl" data-icon="rocket">rocket</span> TECHFLOW
              </div>
              <div className="flex items-center gap-2 font-headline font-extrabold text-xl font-bold">
                <span className="material-symbols-outlined text-2xl" data-icon="cloud">cloud</span> STRATUS
              </div>
              <div className="flex items-center gap-2 font-headline font-extrabold text-xl font-bold">
                <span className="material-symbols-outlined text-2xl" data-icon="bolt">bolt</span> NEXUS
              </div>
              <div className="flex items-center gap-2 font-headline font-extrabold text-xl font-bold">
                <span className="material-symbols-outlined text-2xl" data-icon="layers">layers</span> STACKED
              </div>
              <div className="flex items-center gap-2 font-headline font-extrabold text-xl font-bold">
                <span className="material-symbols-outlined text-2xl" data-icon="shield">shield</span> PROTECT
              </div>
            </div>
          </div>
        </section>

        {/* Bento Intelligence Grid */}
        <section className="py-24 px-6 lg:px-12 max-w-7xl mx-auto border-b border-outline-variant/10">
          <div className="flex flex-col lg:flex-row justify-between items-end mb-16 gap-8">
            <div className="max-w-2xl">
              <h2 className="text-4xl lg:text-5xl font-extrabold font-headline tracking-tighter mb-4 text-on-surface">The Intelligence Layer</h2>
              <p className="text-on-surface-variant text-lg leading-relaxed">We don't just show you numbers; we tell you what they mean for your bottom line.</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-12 gap-8 h-auto lg:h-[600px]">
            {/* Large Feature Card */}
            <div className="md:col-span-12 bg-surface-container-lowest/80 backdrop-blur-sm border border-white/50 rounded-xl p-12 relative overflow-hidden flex flex-col justify-between group shadow-sm">
              <div className="max-w-2xl z-10">
                <h3 className="text-3xl font-extrabold font-headline mb-4 text-on-surface">Deep Learning Trends</h3>
                <p className="text-on-surface-variant leading-relaxed text-lg">Our neural engine scans through millions of data points to identify non-obvious correlations in your sales and inventory cycles.</p>
              </div>
              <div className="relative mt-12 w-full h-80 flex items-center justify-center transform group-hover:scale-105 transition-transform duration-700">
                <img alt="Trend Analysis Data" className="rounded-xl w-full h-full object-cover shadow-lg shadow-black/10" src={updatedgraphImg} />
              </div>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-12 gap-8 h-auto mt-8">
             {/* Bottom Feature Card 1 */}
            <div className="md:col-span-12 bg-surface-container-low/80 backdrop-blur-sm border border-white/50 rounded-xl p-10 flex flex-col md:flex-row justify-between md:items-center group shadow-sm z-10 transition-shadow gap-8">
              <div className="flex-1">
                <div className="flex items-center gap-4 mb-6">
                  <span className="text-xs font-bold px-3 py-1 bg-white rounded-full shadow-sm text-primary">Active Sync</span>
                </div>
                <h3 className="text-3xl font-bold font-headline mb-3 text-on-surface">Menu Intelligence</h3>
                <p className="text-on-surface-variant text-lg max-w-xl">Optimize your offerings based on real-time consumer behavior patterns.</p>
              </div>
              <div className="flex -space-x-4">
                <div className="w-16 h-16 rounded-full border-4 border-white bg-indigo-200 flex justify-center items-center font-bold text-sm text-indigo-700 shadow-sm z-30">MR</div>
                <div className="w-16 h-16 rounded-full border-4 border-white bg-emerald-200 flex justify-center items-center font-bold text-sm text-emerald-700 shadow-sm z-20">JT</div>
                <div className="w-16 h-16 rounded-full bg-slate-200 border-4 border-white flex items-center justify-center text-xs font-bold text-on-surface shadow-sm z-10">+12</div>
              </div>
            </div>
          </div>
        </section>

        {/* Testimonial Slider (Static version for high-end look) */}
        <section className="py-24 px-6 lg:px-12 bg-white/40 backdrop-blur-md relative border-b border-outline-variant/10">
          <div className="max-w-7xl mx-auto">
            <div className="grid lg:grid-cols-2 gap-16 items-center">
              <div>
                <h2 className="text-4xl lg:text-5xl font-extrabold font-headline tracking-tighter mb-8 text-on-surface">Built for the thinkers &amp; makers.</h2>
                <div className="space-y-8">
                  <div className="p-8 bg-surface-container-lowest rounded-2xl border border-slate-100 shadow-sm relative overflow-hidden">
                    <span className="material-symbols-outlined absolute top-6 right-8 text-6xl text-primary/10" data-icon="format_quote">format_quote</span>
                    <p className="text-lg text-on-surface font-body italic mb-6 relative z-10">"Visualize AI changed how we approach our quarterly reviews. We no longer spend weeks building decks; the insights are just there, ready for the board."</p>
                    <div className="flex items-center gap-4 relative z-10">
                      <div className="w-12 h-12 rounded-full bg-slate-200 overflow-hidden flex items-center justify-center font-extrabold text-lg text-slate-500">
                         MC
                      </div>
                      <div>
                        <div className="font-extrabold text-sm text-on-surface">Marcus Chen</div>
                        <div className="text-xs text-on-surface-variant font-medium">CTO at TechFlow</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="relative mt-8 lg:mt-0">
                <div className="absolute inset-0 bg-primary/5 rounded-3xl -rotate-3 scale-105"></div>
                <div className="relative bg-white p-2 rounded-3xl shadow-2xl border border-slate-100">
                  <img alt="User collaboration" className="rounded-2xl w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCPJmtzYfxcgvrQc9qtXB8sTK7AniZUX2HCds8ITdd_SX0_OsfZdoMr_EGonOz0SZrWjCY7rCy_ePKNbfTFGaWoDj2cA58UDYgYUY3fQC9SQ5V2GDvY4x1cs33rabJZLqdBS7ZTpiPTukfJpqwp6ftHUt-3C8lltVnuRLXbHqmIOJErUY5EITRbLJfjgFdRHeArlQrtXeaKSuGC_BNSpPQpOERI0OeKjirGo-EQmFSXTqFw3LA9YqmCi1SEuOumYrmbGHo8MBRoT8R6" />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="py-24 px-6 relative overflow-hidden">
          <div className="absolute inset-0 bg-slate-900 z-0"></div>
          <div className="absolute -right-20 -top-20 w-[600px] h-[600px] bg-primary/20 blur-[150px] opacity-60"></div>
          <div className="absolute -left-20 -bottom-20 w-[600px] h-[600px] bg-indigo-600/10 blur-[150px] opacity-40"></div>
          <div className="max-w-4xl mx-auto relative z-10 text-center">
            <h2 className="text-4xl lg:text-6xl font-extrabold font-headline text-white tracking-tighter mb-8 leading-tight">
              Ready to see what your <span className="text-primary-fixed-dim">data is hiding?</span>
            </h2>
            <p className="text-indigo-200 text-xl mb-12 max-w-2xl mx-auto font-body">
              Join over 500+ restaurants and retail chains using Visualize AI to turn complex datasets into clear growth paths.
            </p>
            <div className="flex flex-col sm:flex-row justify-center gap-6 mt-12">
              <NavLink 
                to={currentUser ? "/app/upload" : "/signup"}
                className="px-12 py-5 rounded-xl bg-white text-primary font-extrabold text-lg shadow-xl shadow-white/5 hover:bg-indigo-50 transition-colors"
              >
                Upload Data
              </NavLink>
            </div>
            <p className="mt-8 text-indigo-300/60 text-sm font-medium">No credit card required. Free 500MB analysis for first-time users.</p>
          </div>
        </section>
      </main>

      {/* Simple Footer */}
      <footer className="bg-surface py-20 px-6 lg:px-12 relative z-10 border-t border-slate-200/50">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-12 pt-4">
          <div className="flex flex-col items-center md:items-start gap-4">
            <span className="text-2xl font-extrabold tracking-tighter font-headline text-on-surface">Visualize AI</span>
            <p className="text-on-surface-variant text-sm font-medium">© 2024 Intelligence Curator. All rights reserved.</p>
          </div>
          <div className="flex gap-10 text-sm font-bold text-on-surface-variant">
            <button 
              onClick={() => setActivePopup('privacy')}
              className="hover:text-primary transition-colors cursor-pointer"
            >
              Privacy
            </button>
          </div>
          <div className="flex gap-4 text-on-surface-variant">
            <button 
              onClick={() => setActivePopup('email')}
              className="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center hover:bg-surface-container-high hover:text-primary transition-colors"
            >
              <span className="material-symbols-outlined text-xl" data-icon="mail">mail</span>
            </button>
          </div>
        </div>
      </footer>

      {/* Glassmorphic Interaction Popup */}
      {activePopup && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-6 animate-in fade-in duration-300">
          <div 
            className="absolute inset-0 bg-on-surface/20 backdrop-blur-sm"
            onClick={() => setActivePopup(null)}
          ></div>
          <div className="relative bg-white/90 backdrop-blur-xl p-10 rounded-[2.5rem] shadow-2xl border border-white/50 max-w-md w-full animate-in zoom-in-95 duration-300">
            <button 
              onClick={() => setActivePopup(null)}
              className="absolute top-6 right-6 w-10 h-10 rounded-full bg-surface flex items-center justify-center hover:bg-primary/10 hover:text-primary transition-colors"
            >
              <span className="material-symbols-outlined text-xl">close</span>
            </button>
            <div className="flex flex-col items-center text-center">
              <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center text-primary mb-6">
                <span className="material-symbols-outlined text-3xl">
                  {activePopup === 'email' ? 'alternate_email' : 'gavel'}
                </span>
              </div>
              <h4 className="text-2xl font-extrabold font-headline text-on-surface mb-2">
                {activePopup === 'email' ? 'Get in Touch' : 'Privacy Notice'}
              </h4>
              <p className="text-on-surface-variant font-medium leading-relaxed">
                {activePopup === 'email' 
                  ? 'Feel free to reach out for any inquiries or support.' 
                  : 'Your data security is our top priority. We comply with all 2026 digital asset guidelines.'}
              </p>
              <div className="mt-8 p-4 bg-primary/5 rounded-xl border border-primary/10 w-full">
                <span className="font-extrabold text-primary break-all">
                  {activePopup === 'email' ? 'rohankapoor@gmail.com' : 'Privacy Notice 2026'}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
