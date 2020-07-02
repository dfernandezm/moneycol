type User = {
    userId: string,
    token: string,
    email: string
}

class LocalStateService {
    setToken(token: string) {
        localStorage.setItem("token", token);
    }

    getToken(): string | null {
        return localStorage.getItem("token");
    }

    getUser(): User | null {
        const localUser = localStorage.getItem("user");
        if (localUser) {
            return JSON.parse(localUser) as User;
        }
        return null;
    }

    setUserFromObject(userObj: User) {
        localStorage.setItem("user", JSON.stringify(userObj));
    }

    clear() {
        localStorage.clear();
    }

    clearToken() {
        localStorage.removeItem("token");
    }

    clearUser() {
        localStorage.removeItem("user");
    }
}

export const localStateService = new LocalStateService();