const submitButton = document.getElementById('submitButton');
const resetButton = document.getElementById('resetButton');
const logName = document.getElementById('logName');
const archiveName = document.getElementById('archiveName');
var user_id = generateUserId();
var messageList = document.getElementById('messageList');
var firstLoad = true;  // Flag to track the first load

function generateUserId() {
    return 'user_' + Math.floor(Math.random() * 1000);
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
        submitButton.removeAttribute('disabled'); // Enable the submit button
    } else {
        archiveName.style.display = 'none';
        submitButton.setAttribute('disabled', 'true'); // Disable the submit button
    }
}

client.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    document.getElementById('logForm').addEventListener('submit', function (event) {
        event.preventDefault();
        resetButton.removeAttribute("disabled");
        submitButton.setAttribute("disabled", 'true');
        logName.setAttribute('disabled', 'true');
        archiveName.setAttribute('disabled', 'true'); // Disable archive dropdown
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

    // Re-enable the dropdowns and reset button states
    logName.removeAttribute('disabled');
    archiveName.removeAttribute('disabled');
    submitButton.setAttribute('disabled', 'true');
    resetButton.setAttribute('disabled', 'true');
    archiveName.style.display = 'none'; // Hide the archive dropdown

    location.reload(true);
});

function logout() {
    var logoutUrl = '/logs/' + user_id + '/logout';
    window.location.href = logoutUrl;
}

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

// Initial call to disable the button until a selection is made
function updateButtonState() {
    if (logName.value !== 'Choose a log to show...') {
        submitButton.removeAttribute('disabled');
    } else {
        submitButton.setAttribute('disabled', 'true');
    }
}

// Event listeners to update button state
logName.addEventListener('change', updateButtonState);
archiveName.addEventListener('change', updateButtonState);
updateButtonState();
