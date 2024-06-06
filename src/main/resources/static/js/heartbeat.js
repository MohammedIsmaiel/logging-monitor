var client;
var webSocket = new SockJS('/websocket');
client = Stomp.over(webSocket);

function generateUserId() {
    return 'user_' + Math.floor(Math.random() * 1000);
}
var user_id = generateUserId();

function sendHeartbeat() {
    fetch('/logs/' + encodeURIComponent(user_id) + '/heartbeat', { method: 'GET' })
        .then(response => {
            if (response.ok) {
                console.log('Heartbeat sent successfully');
            } else {
                console.error('Failed to send heartbeat');
            }
        })
        .catch(error => {
            console.error('Error sending heartbeat:', error);
        });
}
setInterval(sendHeartbeat, 5 * 60 * 1000); // Send heartbeat every 5 minutes

// close the session in refreshing 
window.addEventListener('beforeunload', function () {
    fetch('/logs/' + encodeURIComponent(user_id) + '/logout', { method: 'GET' });
});