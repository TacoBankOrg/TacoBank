import React, { useState } from 'react';

// 서버 상태 타입 정의
interface ServerStatus {
    auth: string;
    business: string;
    ai: string;
    email: string;
}

// 서버 URL 매핑
const SERVER_URLS: Record<keyof ServerStatus, string> = {
    auth: 'http://localhost/auth',
    business: 'http://localhost/business',
    ai: 'http://localhost/ai',
    email: 'http://localhost/email',
};

const HealthCheck: React.FC = () => {
    const [status, setStatus] = useState<ServerStatus>({
        auth: 'Unknown',
        business: 'Unknown',
        ai: 'Unknown',
        email: 'Unknown',
    });

    // 서버 상태를 확인하는 함수
    const checkHealth = async (server: keyof ServerStatus) => {
        try {
            const response = await fetch(`${SERVER_URLS[server]}/health`);
            if (response.status === 200) {
                setStatus((prev) => ({ ...prev, [server]: 'Healthy' }));
            } else {
                setStatus((prev) => ({ ...prev, [server]: 'Unhealthy' }));
            }
        } catch (error) {
            setStatus((prev) => ({ ...prev, [server]: 'Unhealthy' }));
        }
    };

    return (
        <div>
            <button onClick={() => checkHealth('auth')}>Auth Server</button>
            <p>Auth Server: {status.auth}</p>

            <button onClick={() => checkHealth('business')}>Business Server</button>
            <p>Business Server: {status.business}</p>

            <button onClick={() => checkHealth('ai')}>AI Server</button>
            <p>AI Server: {status.ai}</p>

            <button onClick={() => checkHealth('email')}>Email Server</button>
            <p>Email Server: {status.email}</p>
        </div>
    );
};

export default HealthCheck;
