import axios from 'axios';

const api = axios.create({
  baseURL: "http://localhost:8080/api"
});

export const uploadDataset = async (file, name) => {
  const formData = new FormData();
  formData.append("file", file);
  if (name) {
    formData.append("name", name);
  }

  const response = await api.post("/datasets/upload", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data.data;
};

export const getDashboardAnalysis = async (datasetId) => {
  const response = await api.get(`/datasets/${datasetId}/dashboard`);
  return response.data.data;
};

export default api;
