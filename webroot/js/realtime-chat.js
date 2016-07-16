window.addEventListener("DOMContentLoaded", function() {
	// Base URL
	var BASEURL = "http://localhost:7070";
	// UI Elements
	var txtChatId = document.querySelector("#txtChatId");
	var txtChatName = document.querySelector("#txtChatName");
	var txtChatMessage = document.querySelector("#txtChatMessage");
	var ulChatHistory = document.querySelector("#ulChatHistory");
	var pErrorMessage = document.querySelector("#pErrorMessage");
	var btnSend = document.querySelector("#btnSend");
	
	/**
	 * Create List Item for Chat History
	 * @param text Chat Message
	 * @returns
	 */
	function _createListItem(text) {
		var li = document.createElement("li");
		li.innerHTML = text;
		ulChatHistory.appendChild(li);
	};
	
	/**
	 * Load Chat
	 * @returns
	 */
	function _loadChat() {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4) {
				if (xhr.status === 200) {
					if (JSON.parse(xhr.responseText).name.length > 0) {
						_createListItem(
							JSON.parse(xhr.responseText).name + " says " + JSON.parse(xhr.responseText).message
						);
					}
				}
			}
		};
		xhr.open("GET", BASEURL + "/api/chats/" + txtChatId.value);
		xhr.send();
	};
	
	/**
	 * Register Event Bus
	 * @returns
	 */
	function _registerEventbusHandler() {
		var eventBus = new EventBus(BASEURL + "/eventbus");
		eventBus.onopen = function() {
			eventBus.registerHandler("chat." + txtChatId.value, function(error, message) {
				if (error) {
					pErrorMessage.innerHTML += "Error: " + error.message + "\n";
					return;
				}
				_createListItem(
					JSON.parse(message.body).name + " says " + JSON.parse(message.body).message
				);
			});
		}
	};
	
	/**
	 * Send Chat Message
	 * @returns
	 */
	function _sendChatMessage() {
		var chatName = txtChatName.value;
		var chatMessage = txtChatMessage.value;
		
		if (chatName.length <= 0) {
			pErrorMessage.innerHTML += "Empty Chat Name is invalid! <br/>";
			return;
		}
		
		if (chatMessage.length <= 0) {
			pErrorMessage.value += "Empty Chat Message is invalid! <br/>";
			return;
		}
		
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4) {
				if (!xhr.status === 200) {
					pErrorMessage.innerHTML = "Invalid Chat Message!";
				} else {
					txtChatMessage.value = "";
				}
			}
		};
		xhr.open("PATCH", BASEURL + "/api/chats/" + txtChatId.value);
		xhr.setRequestHeader("Content-Type", "application/json");
		xhr.send(JSON.stringify({name: chatName, message: chatMessage}));
	};
	
	/**
	 * Call private Function
	 * @returns
	 */
	function init() {
		_loadChat();
		_registerEventbusHandler();
		btnSend.addEventListener("click", _sendChatMessage, false);
	};
	
	/**
	 * Load Chat and Register Event Listeners
	 */
	init();
});