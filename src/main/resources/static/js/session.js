var sessionTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
var warningTime = 9 * 60 * 1000; // 1 minute before session timeout
var warningDisplayed = false;
var activityTimeout;

function resetActivityTimeout() {
    clearTimeout(activityTimeout);
    activityTimeout = setTimeout(showSessionWarning, warningTime);
}

function showSessionWarning() {
    warningDisplayed = true;
    var warningMessage = "Your session is about to expire. Click OK to keep it alive.";
    if (confirm(warningMessage)) {
        sendHeartbeat(); // Send a heartbeat to the server to keep the session alive
        resetActivityTimeout(); // Reset the activity timeout
        warningDisplayed = false;
    } else {
            // If the user chooses not to keep the session alive, log them out or handle accordingly
            logout();
    }
}

document.addEventListener('mousemove', resetActivityTimeout);
document.addEventListener('keydown', resetActivityTimeout);
resetActivityTimeout(); // Initial call to set the timeout
