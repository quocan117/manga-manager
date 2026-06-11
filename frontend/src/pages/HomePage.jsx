import React from 'react';
import { useNavigate } from 'react-router-dom';

const HomePage = () => {
  const navigate = useNavigate();
  return (
    <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <button 
        onClick={() => navigate('/login')}
        style={{ 
          padding: '12px 24px', 
          fontSize: '16px', 
          cursor: 'pointer', 
          backgroundColor: '#007bff', 
          color: 'white', 
          border: 'none', 
          borderRadius: '4px' 
        }}
      >
        Login
      </button>
    </div>
  );
};

export default HomePage;