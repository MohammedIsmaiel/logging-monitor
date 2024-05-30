var client;
var webSocket = new SockJS('/websocket');
client = Stomp.over(webSocket);

const submitButton = document.getElementById('submitButton');
const resetButton = document.getElementById('resetButton');
const logName = document.getElementById('logName');
const archiveName = document.getElementById('archiveName');
var messageList = document.getElementById('messageList');

function generateUserId() {
    return 'user_' + Math.floor(Math.random() * 1000);
}
var user_id = generateUserId();
// var user_id = sessionStorage.getItem('user_id') || generateUserId();
// sessionStorage.setItem('user_id', user_id);

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

client.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // // Check if there's a log being watched and re-establish the connection if needed
    // var watchedLog = sessionStorage.getItem('watchedLog');
    // if (watchedLog) {
    //     resumeWatchingLog(watchedLog);
    // }

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

        // // Save the current log being watched in session storage
        // sessionStorage.setItem('watchedLog', logNameValue);
        // sessionStorage.setItem('isArchive', isArchive);

        if (!isArchive) {
            client.subscribe(topicPath, function (message) {
                console.log(message.body);
                var item = document.createElement('p');
                item.appendChild(document.createTextNode(message.body));
                item.classList.add('m-0');
                messageList.appendChild(item);
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

// function resumeWatchingLog(logName) {
//     var isArchive = sessionStorage.getItem('isArchive') === 'true';
//     var topicPath = '/topic/messages/' + encodeURIComponent(user_id) + '/';
//     var url = '/logs/' + encodeURIComponent(user_id) + '/';
//     if (isArchive) {
//         topicPath += encodeURIComponent(logName);
//         url += encodeURIComponent(logName) + '?archive=true';
//     } else {
//         topicPath += encodeURIComponent(logName);
//         url += encodeURIComponent(logName) + '?archive=false';
//     }

//     if (!isArchive) {
//         client.subscribe(topicPath, function (message) {
//             console.log(message.body);
//             var item = document.createElement('p');
//             item.appendChild(document.createTextNode(message.body));
//             item.classList.add('m-0');
//             messageList.appendChild(item);
//         });
//     }

//     fetch(url)
//         .then(response => {
//             if (!response.ok) {
//                 throw new Error('Network response was not ok');
//             }
//             return response.text();
//         })
//         .then(data => {
//             console.log('Response:', data);
//             if (isArchive) {
//                 var item = document.createElement('pre');
//                 item.appendChild(document.createTextNode(data));
//                 item.classList.add('m-0');
//                 messageList.innerHTML = ''; // Clear previous content
//                 messageList.appendChild(item);
//             }
//         })
//         .catch(error => {
//             console.error('Error:', error);
//         });
// }

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

    // Clear session storage and reload
    // sessionStorage.removeItem('watchedLog');
    // sessionStorage.removeItem('isArchive');
    location.reload(true);
});
