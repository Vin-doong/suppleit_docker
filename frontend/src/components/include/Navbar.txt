import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const Navbar = () => {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 컴포넌트가 마운트될 때와 localStorage가 변경될 때 실행
  useEffect(() => {
    // localStorage에서 토큰을 확인하여 로그인 상태 업데이트
    const token = localStorage.getItem('accessToken');
    setIsLoggedIn(!!token); // 토큰이 있으면 true, 없으면 false
    
    // localStorage 변경 이벤트 리스너 추가
    const handleStorageChange = () => {
      const token = localStorage.getItem('accessToken');
      setIsLoggedIn(!!token);
    };
    
    window.addEventListener('storage', handleStorageChange);
    
    // 컴포넌트 언마운트 시 이벤트 리스너 제거
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  // 로그아웃 처리 함수
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setIsLoggedIn(false);
    navigate('/');
  };

  return (
    <nav className="bg-white shadow-sm">
      <div className="max-w-screen-xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* 로고 영역 */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center">
              <img
                src="/images/logo.png"
                className="h-8 w-auto"
                alt="SUPPLE IT Logo"
              />
              <span className="ml-2 text-xl font-bold text-gray-900">SUPPLE IT</span>
            </Link>
          </div>

          {/* 네비게이션 메뉴 */}
          <div className="flex items-center space-x-8">
            <Link to="/" className="text-gray-700 hover:text-teal-500 font-medium">홈</Link>
            <Link to="/products" className="text-gray-700 hover:text-teal-500 font-medium">상품</Link>
            <Link to="/notices" className="text-gray-700 hover:text-teal-500 font-medium">공지사항</Link>
            
            {/* 로그인한 경우에만 표시할 메뉴 */}
            {isLoggedIn && (
              <>
                <Link to="/mypage" className="text-gray-700 hover:text-teal-500 font-medium">마이페이지</Link>
                <Link to="/favorites" className="text-gray-700 hover:text-teal-500 font-medium">즐겨찾기</Link>
                <Link to="/schedule" className="text-gray-700 hover:text-teal-500 font-medium">일정관리</Link>
              </>
            )}
          </div>

          {/* 로그인/로그아웃 버튼 */}
          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
              <button 
                onClick={handleLogout} 
                className="rounded-md px-4 py-2 bg-teal-500 text-white hover:bg-teal-600 transition-colors"
              >
                로그아웃
              </button>
            ) : (
              <>
                <button 
                  onClick={() => navigate('/login')} 
                  className="rounded-md px-4 py-2 text-teal-500 border border-teal-500 hover:bg-teal-500 hover:text-white transition-colors"
                >
                  로그인
                </button>
                <Link 
                  to="/signup" 
                  className="rounded-md px-4 py-2 bg-teal-500 text-white hover:bg-teal-600 transition-colors"
                >
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;