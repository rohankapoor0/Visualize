import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadDataset } from '../services/api';

export default function UploadPage() {
  const [dragActive, setDragActive] = useState(false);
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState('idle'); // idle, uploading, success, error
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();

  const handleDrag = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") setDragActive(true);
    else if (e.type === "dragleave") setDragActive(false);
  }, []);

  const validateAndSetFile = (selected) => {
    setErrorMsg('');
    if (!selected) return;
    const name = selected.name.toLowerCase();
    if (name.endsWith('.csv') || name.endsWith('.xlsx')) {
      setFile(selected);
    } else {
      setErrorMsg('Please select a valid .csv or .xlsx file.');
    }
  };

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  }, []);

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const handeUpload = async () => {
    if (!file) return;
    setStatus('uploading');
    try {
      const data = await uploadDataset(file, file.name);
      setStatus('success');
      setTimeout(() => navigate(`/app/dashboard/${data.id}`), 1000);
    } catch (err) {
      console.error(err);
      setStatus('error');
      setErrorMsg('Upload failed. Note: Ensure the backend is running.');
    }
  };

  return (
    <div className="p-8 lg:p-12 max-w-7xl mx-auto font-body">
      <div className="mb-12">
        <h2 className="text-4xl lg:text-5xl font-extrabold tracking-tight font-headline text-on-surface mb-4">Upload Dataset</h2>
        <p className="text-lg text-on-surface-variant max-w-2xl">Upload your raw data file and we'll handle the rest. We support CSV and XLSX formats.</p>
      </div>

      {/* Bento Layout for Upload and Tips */}
      <div className="grid grid-cols-12 gap-8">
        
        {/* Main Upload Zone */}
        <div className="col-span-12 lg:col-span-8 bg-surface-container-lowest rounded-xl p-8 lg:p-12 shadow-sm border border-outline-variant/20 flex flex-col items-center justify-center">
          <div className="w-full relative group cursor-pointer"
               onDragEnter={handleDrag}
               onDragLeave={handleDrag}
               onDragOver={handleDrag}
               onDrop={handleDrop}
               onClick={() => document.getElementById('file-upload').click()}
          >
            <div className={`border-2 border-dashed rounded-xl p-16 flex flex-col items-center justify-center transition-all duration-300
                ${dragActive ? 'border-primary bg-primary/5' : 'border-indigo-200 group-hover:border-primary group-hover:bg-primary/5'}
                ${file && status !== 'error' ? 'border-emerald-500 bg-emerald-50/30' : ''}
               ${status === 'error' ? 'border-red-500 bg-red-50/30' : ''}
              `}>
              <input 
                type="file" 
                id="file-upload" 
                className="hidden" 
                accept=".csv, .xlsx" 
                onChange={handleChange}
                disabled={status === 'uploading' || status === 'success'}
              />
              
              {!file ? (
                <>
                  <div className="w-20 h-20 bg-indigo-50 rounded-full flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                    <span className="material-symbols-outlined text-4xl text-primary font-bold">cloud_upload</span>
                  </div>
                  <h3 className="text-2xl font-bold font-headline mb-2 text-on-surface">Drag and drop your dataset</h3>
                  <p className="text-slate-500 mb-8">or click to browse from your computer</p>
                  <button className="bg-primary text-white px-8 py-4 rounded-xl font-bold text-lg shadow-xl shadow-primary/20 hover:scale-105 transition-transform flex items-center gap-2">
                    <span className="material-symbols-outlined">folder_open</span>
                    Select File
                  </button>
                </>
              ) : (
                <div className="flex flex-col items-center text-center">
                  <h3 className="text-2xl font-bold font-headline text-slate-800">{file.name}</h3>
                  <p className="text-sm font-medium text-slate-500 mt-2">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                  {status === 'idle' && <p className="text-sm text-primary font-bold mt-4 tracking-wide uppercase">Ready to upload</p>}
                </div>
              )}
            </div>
          </div>

          {errorMsg && (
            <div className="w-full mt-6 flex items-center gap-3 text-rose-600 bg-rose-50 p-4 rounded-xl border border-rose-100">
              <span className="material-symbols-outlined">error</span>
              <span className="text-sm font-bold">{errorMsg}</span>
            </div>
          )}

          {file && status !== 'success' && (
            <div className="w-full mt-8 flex justify-center">
              <button 
                onClick={(e) => { e.stopPropagation(); handeUpload(); }}
                disabled={status === 'uploading'}
                className="flex items-center justify-center gap-3 w-full sm:w-auto bg-primary text-white px-10 py-4 rounded-xl font-bold text-lg hover:opacity-90 transition-opacity shadow-lg shadow-primary/20 disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {status === 'uploading' ? (
                  <>
                    <span className="material-symbols-outlined animate-spin">refresh</span>
                    Generating Analysis...
                  </>
                ) : (
                  <>
                    Upload and Analyze
                  </>
                )}
              </button>
            </div>
          )}

          <div className="w-full mt-12 flex items-center gap-4 p-6 bg-surface-container-low rounded-xl border border-outline-variant/10">
            <span className="material-symbols-outlined text-primary">info</span>
            <p className="text-sm text-on-surface-variant font-medium">Maximum file size is 50MB. Large datasets may take a few minutes to process.</p>
          </div>
        </div>

        {/* Side Information Column */}
        <div className="col-span-12 lg:col-span-4 space-y-8">
          {/* Format Card */}
          <div className="bg-primary rounded-xl p-8 text-white relative overflow-hidden shadow-lg shadow-primary/20">
            <div className="relative z-10">
              <h4 className="text-xl font-extrabold font-headline mb-4">Supported Formats</h4>
              <div className="space-y-4 font-medium">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center font-bold text-xs uppercase tracking-wider">CSV</div>
                  <span className="text-sm opacity-90">Comma Separated Values</span>
                </div>
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center font-bold text-xs uppercase tracking-wider">XLSX</div>
                  <span className="text-sm opacity-90">Microsoft Excel Sheet</span>
                </div>
              </div>
            </div>
            <img alt="Data visualization dashboard" className="absolute inset-0 w-full h-full object-cover opacity-30 mix-blend-overlay" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDnWo6WAbPmxGB2z7YNAOYpVYl-JD17LOt9dj_l_xW-18HYYaMvyxQQsInIbvi1S6LM7OtSlsSMFTn5XUEC0OuG2mNUxoXrmsySoRbr3daZZnFfg_YCSFKw8NjAix4tbbhYS13W1eKoDgDwEgT6FnmLJZhv1GsXbXOUq-FBaSYrlTFVY7be5CQzQzXZDdAiBqfkSo17AUCIYtbHcgO-w2bK4d_uebHehKE-pZAEymOnJRB7UJZJQv_sBQ1MrxwSKAMHYzy2X69oFAj5" />
          </div>

          {/* Processing Guidelines */}
          <div className="bg-surface-container-lowest rounded-xl p-8 border border-outline-variant/20 shadow-sm">
            <h4 className="font-extrabold font-headline text-lg mb-6 text-on-surface">Processing Guidelines</h4>
            <ul className="space-y-6">
              <li className="flex gap-4">
                <span className="material-symbols-outlined text-tertiary">check_circle</span>
                <div>
                  <p className="font-bold text-sm text-on-surface">Header Row</p>
                  <p className="text-xs text-slate-500 font-medium mt-1">Ensure the first row contains column names.</p>
                </div>
              </li>
              <li className="flex gap-4">
                <span className="material-symbols-outlined text-tertiary">check_circle</span>
                <div>
                  <p className="font-bold text-sm text-on-surface">Consistent Data</p>
                  <p className="text-xs text-slate-500 font-medium mt-1">Keep data types uniform within columns.</p>
                </div>
              </li>
              <li className="flex gap-4">
                <span className="material-symbols-outlined text-tertiary">check_circle</span>
                <div>
                  <p className="font-bold text-sm text-on-surface">No Empty Cells</p>
                  <p className="text-xs text-slate-500 font-medium mt-1">Minimize blank cells for better AI training.</p>
                </div>
              </li>
            </ul>
          </div>

          {/* AI Status Chip */}
          <div className="bg-surface-container rounded-xl p-6 flex flex-wrap items-center justify-between border border-primary/10 gap-4">
            <div className="flex items-center gap-3">
              <span className="material-symbols-outlined text-primary">auto_awesome</span>
              <span className="text-sm font-extrabold text-on-surface">AI Auto-Cleaner Ready</span>
            </div>
            <span className="px-3 py-1 bg-white rounded-full text-[10px] font-bold text-primary uppercase tracking-widest border border-primary/20 shadow-sm">Active</span>
          </div>
        </div>
      </div>
    </div>
  );
}
