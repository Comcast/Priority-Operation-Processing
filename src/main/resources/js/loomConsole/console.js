"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var makeCrossCompatible = function (exported, nameSpace) {
    //Support both browser and node with one build
    if (typeof module !== "undefined" && module.exports) {
       module.exports = exported;
    }
    else if (window) {
        window[nameSpace] = exported;
    }
};
var ConsoleJS = /** @class */ (function () {
    function ConsoleJS() {
        var _this = this;
        //Considering SDK related source can be triggered from both SDK & custom command webpage
        this.consoleWindow = window.opener || window.parent;
        this.createCustomEvent = function (detail) {
            window.dispatchEvent(new CustomEvent(detail.method, { detail: detail }));
        };
        window.addEventListener("message", function (e) {
            //Dispatch custom events to take care of async responses
            _this.createCustomEvent(e.data);
        });
    }
    ConsoleJS.prototype.postToConsole = function (method, args) {
        var consolePostReq = {
            method: method,
            arguments: args
        };
        if (ConsoleJS.isEmbedded()) {
            this.consoleWindow.postMessage(consolePostReq, "*");
        }
        else {
            this.createCustomEvent({
                method: method,
                error: "method \"" + method + "\" cannot be executed. Window must be in a console iframe or opened by a console custom command"
            });
        }
    };
    ConsoleJS.prototype.askConsole = function (method, args) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            window.addEventListener(method, // wait for the custom event to respond before resolving
            function (_a) {
                var detail = _a.detail;
                var response = detail;
                if (response && response.data) {
                    resolve(response.data);
                }
                else {
                    reject(response);
                }
            }, { once: true });
            _this.postToConsole(method, args);
        });
    };
    // All public methods goes below
    ConsoleJS.prototype.getContext = function () {
        return this.askConsole("getContext");
    };
    ConsoleJS.prototype.keepAlive = function () {
        return this.askConsole("keepAlive");
    };
    ConsoleJS.isEmbedded = function () { return top !== self || window.opener; };
    return ConsoleJS;
}());
exports.default = ConsoleJS;
makeCrossCompatible(ConsoleJS, "ConsoleJS");
//# sourceMappingURL=console.js.map