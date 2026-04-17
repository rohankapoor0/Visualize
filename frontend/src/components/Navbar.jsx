import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { userData } = useAuth();
  
  return (
    <header className="sticky top-0 z-40 bg-white/80 backdrop-blur-xl flex justify-between items-center h-20 px-12 shadow-sm shadow-slate-200/50">
      <div className="relative w-96 hidden sm:block">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">search</span>
        <input 
          className="w-full pl-12 pr-4 py-2 bg-surface-container-low border-none rounded-full text-sm focus:ring-2 focus:ring-primary/40 outline-none text-on-surface" 
          placeholder="Search insights..." 
          type="text"
        />
      </div>
      
      <div className="flex items-center gap-6 ml-auto">
        <div className="flex items-center gap-3">
          <div className="text-right hidden md:block">
            <p className="text-sm font-bold font-headline text-slate-900 leading-none">{userData?.name || 'User'}</p>
            <p className="text-xs text-slate-500 font-medium mt-0.5">{userData?.plan || 'Free Plan'}</p>
          </div>
          <div className="w-10 h-10 rounded-full border-2 border-primary/10 bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold shadow-sm">
            {userData?.name?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U'}
          </div>
        </div>
      </div>
    </header>
  );
}
