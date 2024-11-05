import { useState } from "react";


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
        <>
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
        <div>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356747970&to=1730358547970&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=1&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356777787&to=1730358577787&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=2&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356797895&to=1730358597895&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=3&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356804492&to=1730358604492&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=4&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356814494&to=1730358614495&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=5&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        <iframe src="http://localhost:3000/d-solo/be2hf6gx8ndhce/tacobank?from=1730356824495&to=1730358624495&timezone=browser&refresh=auto&showCategory=Panel%20options&orgId=1&panelId=6&__feature.dashboardSceneSolo" width="450" height="200" frameBorder="0"></iframe>
        </div>
        </>
       
    );
};

export default HealthCheck;
