var client;
var webSocket = new SockJS('/websocket');
client = Stomp.over(webSocket);

const submitButton = document.getElementById('submitButton');
const homeButton = document.getElementById('resetButton');
const logName = document.getElementById('logName');

function generateUserId() {
    return 'user_' + Math.floor(Math.random() * 1000);
}

function logout() {
    var logoutUrl = 'http://localhost:8080/logout';
    window.location.href = logoutUrl;
}

function reset() {
    var resetUrl = 'http://localhost:8080/logs';
    window.location.href = resetUrl;
}

var user_id = generateUserId();

var messageList = document.getElementById('messageList');
client.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // Modify the client.subscribe line to include the logName
    document.getElementById('logForm').addEventListener('submit', function (event) {
        event.preventDefault(); // Prevent the default form submission behavior
        homeButton.removeAttribute("disabled");
        messageList.innerHTML = "";
        var logNameValue = document.getElementById('logName').value;

        console.log("TOPIC: ", '/topic/messages/' + encodeURIComponent(user_id) + '/' + encodeURIComponent(logNameValue));
        client.subscribe('/topic/messages/' + encodeURIComponent(user_id) + '/' + encodeURIComponent(logNameValue), function (message) {
            console.log(message.body);
            var item = document.createElement('p');
            item.appendChild(document.createTextNode(message.body));
            messageList.appendChild(item);
        });

        var url = '/logs/' + encodeURIComponent(user_id) + '/' + encodeURIComponent(logNameValue);

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(data => {
                console.log('Response:', data);
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });
});

const resetButton = document.getElementById('resetButton');
resetButton.addEventListener('click', function (event) {
    event.preventDefault();
    var logName = document.getElementById('logName').value;
    var stopUrl = '/logs/' + encodeURIComponent(user_id) + '/' + encodeURIComponent(logName) + '/stop';

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
