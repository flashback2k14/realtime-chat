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
	 * Show Toast
	 * @param msg Error Message
	 * @returns
	 */
	function _showErrorToast(msg) {
		errorToast.MaterialSnackbar.showSnackbar({message: msg});
	};

	/**
	 * Create List Item for Chat History
	 * @param name Chat Name
	 * @param msg Chat Message
	 * @returns
	 */
	function _createListItem(name, msg) {
		var wrapper = document.createElement("div");
		wrapper.classList.add("message__card");
		
		var header = document.createElement("div");
		
		var heading = document.createElement("p");
		heading.classList.add();
		heading.innerHTML = "<b><em>" + name + " says:</em></b>";
		
		var content = document.createElement("div");
		content.classList.add("message__card--content");
		content.innerHTML = msg;
		
		header.appendChild(heading);
		wrapper.appendChild(header);
		wrapper.appendChild(content);
		
		ulChatHistory.appendChild(wrapper);
	};

	/**
	 * Scroll to last Chat Message
	 * @returns
	 */
	function _scrollToBottom() {
		// get all message cards
		var items = document.querySelectorAll(".message__card");
		// scroll into last card
		items[items.length - 1].scrollIntoView(true);
	};

	/**
	 * Initial Load Chat
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
								_createListItem(el.author, el.content);
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
				_createListItem(JSON.parse(message.body).author, JSON.parse(message.body).content);
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
					// clear input
					txtChatMessage.value = "";
					// remove classes to reset the layout
					fiContainerMessage.classList.remove("is-focused");
					fiContainerMessage.classList.remove("is-dirty");
					// set focus to message field
					txtChatMessage.focus();
					// scroll to last chat message
					_scrollToBottom();
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