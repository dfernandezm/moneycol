//TODO: local caching of non-firebase user
class TokensCache {
    private tokenCache = new Map<string, string>();
    private currentUserCache = new Map<string, firebase.User>();
    
    cacheToken(userId:string, token: string) {
        this.tokenCache.set(userId, token);
    }

    readTokenForUserId(userId:string ): string | undefined {
        return this.tokenCache.get(userId);
    }

    cacheCurrentUser(userId: string, user: firebase.User) {
        this.currentUserCache.set(userId, user);
    }

    getCurrentUserFromCache(userId: string) {
        return this.currentUserCache.get(userId);
    }
}

export const tokensCache = new TokensCache();