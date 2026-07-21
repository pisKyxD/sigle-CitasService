const axios = require('axios');
const LISTAS_URL = process.env.LISTAS_SERVICE_URL || 'https://sigle-listasservice-h2eo.onrender.com';

const claimCandidato = async (especialidad, excluirIds = []) => {
  try {
    const res = await axios.post(`${LISTAS_URL}/api/listas/candidato`, { especialidad, excluirIds }, { timeout: 8000 });
    return res.data;
  } catch (err) {
    if (err.response?.status === 404) return null;
    console.error('[ListasClient] Error al reservar candidato:', err.message);
    return null;
  }
};

const resolverOferta = async (listaEsperaId, nuevoEstado) => {
  try {
    await axios.put(`${LISTAS_URL}/api/listas/${listaEsperaId}/oferta`, { nuevoEstado }, { timeout: 8000 });
    return true;
  } catch (err) {
    console.error(`[ListasClient] Error al resolver oferta de lista ${listaEsperaId}:`, err.message);
    return false;
  }
};

module.exports = { claimCandidato, resolverOferta };