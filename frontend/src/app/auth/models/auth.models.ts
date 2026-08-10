export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  message: string;
}

export interface AuthUser {
  username: string;
  role: 'ROLE_ADMIN' | 'ROLE_ANALYST';
  email?: string;
}
