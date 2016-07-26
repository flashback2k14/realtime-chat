window.addEventListener("DOMContentLoaded", function() {
	// Base URL
	var BASEURL = window.location.hostname.indexOf("herokuapp") > 0 
					? "https://vertx-realtime-chat.herokuapp.com"
					: "http://localhost:7070";
	// UI Elements
	var txtChatId = document.querySelector("#txtChatId");
	var txtChatName = document.querySelector("#txtChatName");
	var txtChatMessage = document.querySelector("#txtChatMessage");
	var ulChatHistory = document.querySelector("#ulChatHistory");
	var pErrorMessage = document.querySelector("#pErrorMessage");
	var btnSend = document.querySelector("#btnSend");

	/**
	 * Clear Error Message
	 * 
	 * @returns
	 */
	function _clearErrorMessage() {
		setTimeout(function() {
			pErrorMessage.textContent = "";
		}, 5000);
	};

	/**
	 * Create List Item for Chat History
	 * 
	 * @param text
	 *            Chat Message
	 * @returns
	 */
	function _createListItem(text) {
		var li = document.createElement("li");
		li.innerHTML = text;
		ulChatHistory.appendChild(li);
	};

	/**
	 * Load Chat
	 * 
	 * @returns
	 */
	function _loadChat() {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4) {
				if (xhr.status === 200) {
					if (JSON.parse(xhr.responseText).chatId.length > 0) {
						JSON.parse(xhr.responseText).messages
							.forEach(function(el) {
								_createListItem(
										el.author + " says " + el.content
								);
							});
					}
				}
			}
		};
		xhr.open("GET", BASEURL + "/api/chats/"	+ txtChatId.value);
		xhr.send();
	};

	/**
	 * Register Event Bus
	 * 
	 * @returns
	 */
	function _registerEventbusHandler() {
		var eventBus = new EventBus(BASEURL + "/eventbus");
		eventBus.onopen = function() {
			eventBus.registerHandler("chat." + txtChatId.value, function(error, message) {
				if (error) {
					pErrorMessage.innerHTML += "Error: " + error.message + "<br/>";
					_clearErrorMessage();
					return;
				}
				_createListItem(
						JSON.parse(message.body).author
						+ " says "
						+ JSON.parse(message.body).content
				);
			});
		}
	};

	/**
	 * Send Chat Message
	 * 
	 * @returns
	 */
	function _sendChatMessage() {
		var chatName = txtChatName.value;
		var chatMessage = txtChatMessage.value;

		if (chatName.length <= 0) {
			pErrorMessage.innerHTML += "Empty Chat Name is invalid! <br/>";
			_clearErrorMessage();
			return;
		}

		if (chatMessage.length <= 0) {
			pErrorMessage.value += "Empty Chat Message is invalid! <br/>";
			_clearErrorMessage();
			return;
		}

		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4) {
				if (!xhr.status === 200) {
					pErrorMessage.innerHTML += "Invalid Chat Message! <br/>";
					_clearErrorMessage();
				} else {
					txtChatMessage.value = "";
				}
			}
		};
		xhr.open("POST", BASEURL + "/api/chats/" + txtChatId.value + "/messages")
		xhr.setRequestHeader("Content-Type", "application/json");
		xhr.send(JSON.stringify({
			author : chatName,
			content : chatMessage
		}));
	};

	/**
	 * Call private Function
	 * 
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