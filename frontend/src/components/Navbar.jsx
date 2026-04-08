import { NavLink } from 'react-router-dom';
import { Bell, Search } from 'lucide-react';

export default function Navbar() {
  return (
    <header className="h-16 bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-10 flex items-center justify-between px-8">
      <div className="flex items-center gap-4 text-slate-400">
        <Search size={20} className="hidden sm:block" />
        <input 
          type="text" 
          placeholder="Search for datasets..." 
          className="bg-transparent border-none text-sm text-slate-700 focus:outline-none hidden sm:block w-64"
        />
      </div>
      
      <div className="flex items-center gap-4">
        <button className="text-slate-400 hover:text-slate-600 transition-colors p-2 rounded-full hover:bg-slate-50">
          <Bell size={20} />
        </button>
        <NavLink 
          to="/app/upload" 
          className="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-medium hover:bg-indigo-700 transition-colors shadow-sm"
        >
          New Analysis
        </NavLink>
      </div>
    </header>
  );
}
