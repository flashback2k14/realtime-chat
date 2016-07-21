window.addEventListener("DOMContentLoaded", function() {
	// Base URL
	var BASEURL = window.location.hostname.indexOf("herokuapp") > 0 ? "https://vertx-realtime-chat.herokuapp.com" : "http://localhost:7070";
	// UI Elements
	var txtChatId = document.querySelector("#txtChatId");
	var txtChatName = document.querySelector("#txtChatName");
	var fiContainerMessage = document.querySelector("#fiContainerMessage");
	var txtChatMessage = document.querySelector("#txtChatMessage");
	var ulChatHistory = document.querySelector("#ulChatHistory");
	var errorToast = document.querySelector("#errorToast");
	var btnSend = document.querySelector("#btnSend");
	
	/**
	 * Show Toast
	 * @param msg Error Message
	 * @returns
	 */
	function _showErrorToast(msg) {
		errorToast.MaterialSnackbar.showSnackbar({message: msg});
	};
	
	/**
	 * Create List Item for Chat History
	 * @param text Chat Message
	 * @returns
	 */
	function _createListItem(text) {
		var li = document.createElement("li");
		li.classList.add("mdl-typography--title", "item--format");
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
							JSON.parse(xhr.responseText).name + " <b><em>says</em></b> " + JSON.parse(xhr.responseText).message
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
					_showErrorToast("Error: " + error.message);
					return;
				}
				_createListItem(
					JSON.parse(message.body).name + " <b><em>says</em></b> " + JSON.parse(message.body).message
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