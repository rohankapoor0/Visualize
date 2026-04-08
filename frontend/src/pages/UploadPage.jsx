import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { UploadCloud, FileType, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
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
      setErrorMsg('Upload failed. Note: Ensure the backend is running on localhost:8080.');
    }
  };

  return (
    <div className="max-w-3xl mx-auto pt-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900">Upload Dataset</h1>
        <p className="text-slate-500 mt-2">Upload your raw data file and we'll handle the rest. We support CSV and XLSX formats.</p>
      </div>

      <div className="bg-white rounded-3xl shadow-sm border border-slate-200 p-8">
        
        <div 
          className={`relative flex flex-col items-center justify-center p-12 border-2 border-dashed rounded-2xl transition-colors cursor-pointer
            ${dragActive ? 'border-indigo-500 bg-indigo-50/50' : 'border-slate-300 hover:border-slate-400 bg-slate-50/50'}
            ${file && status !== 'error' ? 'border-emerald-500 bg-emerald-50/30' : ''}
            ${status === 'error' ? 'border-red-500 bg-red-50/30' : ''}
          `}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
          onClick={() => document.getElementById('file-upload').click()}
        >
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
              <div className="w-16 h-16 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-4 shadow-sm">
                <UploadCloud size={32} />
              </div>
              <p className="text-lg font-medium text-slate-700">Click to select or drag and drop</p>
              <p className="text-sm text-slate-500 mt-1">.CSV or .XLSX up to 50MB</p>
            </>
          ) : (
            <div className="flex flex-col items-center text-center">
              {status === 'success' ? (
                <CheckCircle2 size={48} className="text-emerald-500 mb-4" />
              ) : status === 'error' ? (
                <AlertCircle size={48} className="text-red-500 mb-4" />
              ) : (
                <FileType size={48} className="text-indigo-500 mb-4" />
              )}
              
              <h3 className="text-lg font-medium text-slate-800">{file.name}</h3>
              <p className="text-sm text-slate-500 mt-1">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
              
              {status === 'idle' && (
                <p className="text-sm text-indigo-600 font-medium mt-4">Ready to upload</p>
              )}
            </div>
          )}
        </div>

        {errorMsg && (
          <div className="mt-6 flex items-center gap-2 text-red-600 bg-red-50 p-4 rounded-xl border border-red-100">
            <AlertCircle size={20} />
            <span className="text-sm font-medium">{errorMsg}</span>
          </div>
        )}

        {file && status !== 'success' && (
          <div className="mt-8 flex justify-end">
            <button 
              onClick={(e) => { e.stopPropagation(); handeUpload(); }}
              disabled={status === 'uploading'}
              className="flex items-center gap-2 bg-indigo-600 text-white px-8 py-3 rounded-xl font-medium hover:bg-indigo-700 transition-colors shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {status === 'uploading' ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  Generating Analysis...
                </>
              ) : (
                'Upload and Analyze'
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
