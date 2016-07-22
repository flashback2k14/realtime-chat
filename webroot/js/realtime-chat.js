window.addEventListener("DOMContentLoaded", function() {
	// Base URL
	var BASEURL = window.location.hostname.indexOf("herokuapp") > 0 
					? "https://vertx-realtime-chat.herokuapp.com"
					: "http://localhost:7070";

	// UI Elements
	var txtChatId = document.querySelector("#txtChatId");
	var txtChatName = document.querySelector("#txtChatName");
	var fiContainerMessage = document.querySelector("#fiContainerMessage");
	var txtChatMessage = document.querySelector("#txtChatMessage");
	var ulChatHistory = document.querySelector("#ulChatHistory");
	var errorToast = document.querySelector("#errorToast");
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
	function _createListItem(name, msg) {
		var li = document.createElement("li");
		li.classList.add("mdl-typography--title", "item--format");
		li.innerHTML = "&bull; &emsp; " + name + " <b><em>says</em></b> " + msg;
		ulChatHistory.appendChild(li);
	};

	/**
	 * Load Chat
	 * 
	 * @returns
	 */
	function _initLoadChat() {
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
					_showErrorToast("Error: " + error.message);
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
			_showErrorToast("Empty Chat Name is invalid!");
			return;
		}

		if (chatMessage.length <= 0) {
			_showErrorToast("Empty Chat Message is invalid!");
			return;
		}

		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4) {
				if (!xhr.status === 200) {
					_showErrorToast("Invalid Chat Message!");
				} else {
					txtChatMessage.value = "";
					fiContainerMessage.classList.remove("is-focused");
					fiContainerMessage.classList.remove("is-dirty");
					txtChatMessage.focus();
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
		_initLoadChat();
		_registerEventbusHandler();
		btnSend.addEventListener("click", _sendChatMessage, false);
	};

	/**
	 * Load Chat and Register Event Listeners
	 */
	init();
});