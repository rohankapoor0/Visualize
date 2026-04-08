import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import UploadPage from './pages/UploadPage';
import DashboardPage from './pages/DashboardPage';
import Layout from './components/Layout';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        
        {/* Dashboard Layout Routes */}
        <Route path="/app" element={<Layout />}>
          <Route path="upload" element={<UploadPage />} />
          <Route path="dashboard/:datasetId" element={<DashboardPage />} />
          <Route path="dashboard/:datasetId/insights" element={<DashboardPage />} />
          <Route path="dashboard/:datasetId/flowchart" element={<DashboardPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
