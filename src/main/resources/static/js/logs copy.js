var client;
var webSocket = new SockJS('/websocket');
client = Stomp.over(webSocket);
const submitButton = document.getElementById('submitButton');
const resetButton = document.getElementById('resetButton');
const logName = document.getElementById('logName');
const archiveName = document.getElementById('archiveName');
var messageList = document.getElementById('messageList');
var firstLoad = true;  // Flag to track the first load
var sessionTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
var warningTime = 9 * 60 * 1000; // 1 minute before session timeout
var warningDisplayed = false;
var activityTimeout;

function generateUserId() {
    return 'user_' + Math.floor(Math.random() * 1000);
}
var user_id = generateUserId();

function logout() {
    var logoutUrl = '/logs/' + user_id + '/logout';
    window.location.href = logoutUrl;
}

function loadArchives() {
    var logNameValue = logName.value;
    if (logNameValue !== 'Choose a log to show...') {
        fetch('/logs/' + encodeURIComponent(logNameValue) + '/archives')
        .then(response => response.json())
        .then(data => {
            if (data.length > 0) {
                archiveName.style.display = 'block';
                archiveName.innerHTML = '<option selected>Choose an archive...</option>';
                // Sort archive names alphabetically
                data.sort();
                data.forEach(archive => {
                    var option = document.createElement('option');
                    option.value = archive;
                    option.text = archive;
                    archiveName.appendChild(option);
                });
            } else {
                archiveName.style.display = 'none';
            }
        })
        .catch(error => {
            console.error('Error fetching archives:', error);
        });
    } else {
        archiveName.style.display = 'none';
    }
}

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
    } 
}

document.addEventListener('mousemove', resetActivityTimeout);
document.addEventListener('keydown', resetActivityTimeout);
resetActivityTimeout(); // Initial call to set the timeout

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
function autoScroll() {
    messageList.scrollTop = messageList.scrollHeight;
}

function markAsRead() {
    const unreadMessages = messageList.querySelectorAll('.unread');
    unreadMessages.forEach(message => {
        message.classList.remove('unread');
    });
}
function checkScroll() {
    if (messageList.scrollTop + messageList.clientHeight >= messageList.scrollHeight) {
        markAsRead();
    }
}
client.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    document.getElementById('logForm').addEventListener('submit', function (event) {
        event.preventDefault();
        resetButton.removeAttribute("disabled");
        submitButton.setAttribute("disabled", 'true');
        logName.setAttribute('disabled', 'true');
        messageList = document.getElementById('messageList');
        var logNameValue = document.getElementById('logName').value;
        var archiveNameValue = document.getElementById('archiveName').value;
        var topicPath = '/topic/messages/' + encodeURIComponent(user_id) + '/';
        var url = '/logs/' + encodeURIComponent(user_id) + '/';
        var isArchive = archiveNameValue !== 'Choose an archive...' && archiveNameValue !== null;
        if (isArchive) {
            topicPath += encodeURIComponent(archiveNameValue);
            url += encodeURIComponent(archiveNameValue) + '?archive=true';
        } else {
            topicPath += encodeURIComponent(logNameValue);
            url += encodeURIComponent(logNameValue) + '?archive=false';
        }

        if (!isArchive) {
            client.subscribe(topicPath, function (message) {
                console.log(message.body);
                var item = document.createElement('p');
                item.appendChild(document.createTextNode(message.body));
                item.classList.add('m-0', 'unread');
                messageList.appendChild(item);
                if (firstLoad) {  // Auto-scroll only on the first load
                    autoScroll();
                    firstLoad = false;  // Set the flag to false after the first load
                }
            });
        }

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(data => {
                console.log('Response:', data);
                if (isArchive) {
                    var item = document.createElement('pre');
                    item.appendChild(document.createTextNode(data));
                    item.classList.add('m-0');
                    messageList.innerHTML = ''; // Clear previous content
                    messageList.appendChild(item);
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });
});

resetButton.addEventListener('click', function (event) {
    event.preventDefault();
    var logNameValue = document.getElementById('logName').value;
    var stopUrl = '/logs/' + encodeURIComponent(user_id) + '/' + encodeURIComponent(logNameValue) + '/stop';
    fetch(stopUrl, {
        method: 'GET'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(data => {
            console.log('Stop Response:', data);
        })
        .catch(error => {
            console.error('Error stopping log:', error);
        });

    location.reload(true);
});
