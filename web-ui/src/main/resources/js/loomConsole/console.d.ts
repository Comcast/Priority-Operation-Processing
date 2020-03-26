interface IConsoleJS {
    getContext(): Promise<IConsoleResponse>;
    keepAlive(): Promise<IConsoleResponse>;
}
declare type MessageType = "getContext" | "keepAlive";
interface IConsoleResponse {
    method: MessageType;
    data?: any;
    error?: string;
}
export default class ConsoleJS implements IConsoleJS {
    constructor();
    private consoleWindow;
    private static isEmbedded;
    private postToConsole;
    private createCustomEvent;
    private askConsole;
    getContext(): Promise<IConsoleResponse>;
    keepAlive(): Promise<IConsoleResponse>;
}
export {};
