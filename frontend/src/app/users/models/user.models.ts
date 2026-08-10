export type UserRole = 'ROLE_ADMIN' | 'ROLE_ANALYST' | 'ADMIN' | 'ANALYST';

export interface UserAccount {
  id?: number;
  username: string;
  email?: string;
  password?: string;
  role: UserRole;
  active: boolean;
  createdAt?: string;
}
