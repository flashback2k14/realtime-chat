window.addEventListener("DOMContentLoaded", function() {
	/**
	 * Base URL
	 */
	var BASEURL = window.location.hostname.indexOf("herokuapp") > 0 
										? "https://vertx-realtime-chat.herokuapp.com"
										: "http://localhost:7070";
	
	/**
	 * Get UI Elements from Document
	 */
	var txtChatId = document.querySelector("#txtChatId");
	var fiContainerId = document.querySelector("#fiContainerId");
	var btnClearCombobox = document.querySelector("#btnClearCombobox");
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
	 * @param timestamp Chat Message created time
	 * @returns
	 */
	function _createListItem(name, msg, timestamp) {
		// wrapper element
		var wrapper = document.createElement("div");
		wrapper.classList.add("message__card");
		// heading element
		var header = document.createElement("div");
		var heading = document.createElement("p");
		heading.innerHTML = "<b><em>" + name + " says:</em></b>";
		// content message element
		var content = document.createElement("div");
		content.classList.add("message__card--content");
		content.innerHTML = msg;
		// content timestamp element
		var createdAt = document.createElement("p");
		createdAt.classList.add("message__card--created", "mdl-typography--text-right");
		// convert timestamp into readable time
		timeStampOpts = {
				year:"numeric", month:"numeric", day:"numeric",
				hour:"numeric", minute:"numeric", second: "numeric"
		}
		createdAt.innerHTML = new Intl.DateTimeFormat("en-US", timeStampOpts)
															.format(new Date().setTime(timestamp));
		// add elements to the parents
		header.appendChild(heading);
		wrapper.appendChild(header);
		content.appendChild(createdAt);
		wrapper.appendChild(content);
		// add list item to the list view
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
	 * Requester Data from Server
	 * @param {String} route
	 * @param {Object} body
	 * @returns Promise
	 */
	function _dataRequester(route, body) {
		return new Promise(function(resolve, reject) {
			// create xhr object
			var xhr = new XMLHttpRequest();
			// init listener
			xhr.onreadystatechange = function() {
				if (xhr.readyState === 4) {
					// check xhr status
					if (xhr.status === 200) {
						resolve(JSON.parse(xhr.responseText));
					} else if (xhr.status === 201) {
						resolve();
					} else {
						reject(new Error(xhr.status + ": " + xhr.statusText));
					}
				}
			};
			// check if body object is available
			// 	- Yes --> make a POST request
			//	- No  --> make a GET request
			if (body) {
				xhr.open("POST", BASEURL + route)
				xhr.setRequestHeader("Content-Type", "application/json");
				xhr.send(JSON.stringify(body));
			} else {
				xhr.open("GET", BASEURL + route);
				xhr.send();
			}
		});
	};

	/**
	 * Load Chat
	 * @returns
	 */
	function _loadChat() {
		_dataRequester("/api/chats/" + txtChatId.value)
			.then(function(response) {
				response.messages.forEach(function(message) {
					_createListItem(message.author, message.content, message.created);
				});
			})
			.catch(function(error) {
				_showErrorToast(error.message);
			});
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
				var json = JSON.parse(message.body);
				if (json) {
					_createListItem(json.author, json.content, json.created);
				}
			});
		}
	};

	/**
	 * Clear List View from Chat Messages
	 * @returns
	 */
	function _clearListView() {
		if (ulChatHistory.childNodes.length > 0) {
			ulChatHistory.innerHTML = "";
		}
	};

	/**
	 * Clear Combobox and List View
	 * @returns
	 */
	function _clearComboBox() {
		// clear input
		txtChatId.value = "";
		// remove classes to reset the layout
		fiContainerId.classList.remove("is-focused");
		fiContainerId.classList.remove("is-dirty");
		// clear list view
		_clearListView();
	};

	/**
	 * Handle changed Chat IDs
	 * 	- clear List View
	 * 	- Load Chat messages for specific Chat ID
	 * 	- Register Eventbus for specific Chat ID
	 * @returns
	 */
	function _chatIdChanged() {
		_clearListView();
		_loadChat();
		_registerEventbusHandler();
	};

	/**
	 * Send Chat Message
	 * @returns
	 */
	function _sendChatMessage() {
		// get Chat Name and Chat Message
		var chatName = txtChatName.value;
		var chatMessage = txtChatMessage.value;
		// check that Chat Name is not Empty
		if (chatName.length <= 0) {
			_showErrorToast("Empty Chat Name is invalid!");
			return;
		}
		// check that Chat Message is not Empty
		if (chatMessage.length <= 0) {
			_showErrorToast("Empty Chat Message is invalid!");
			return;
		}
		// create body Object for sending to the server
		var body = {
			author: chatName, 
			content: chatMessage
		};
		// send message
		_dataRequester("/api/chats/" + txtChatId.value + "/messages", body)
			.then(function() {
				// clear input
				txtChatMessage.value = "";
				// remove classes to reset the layout
				fiContainerMessage.classList.remove("is-focused");
				fiContainerMessage.classList.remove("is-dirty");
				// set focus to message field
				txtChatMessage.focus();
				// scroll to last chat message
				_scrollToBottom();
			})
			.catch(function(error) {
				_showErrorToast("Invalid Chat Message! " + error.message);
			});
	};

	/**
	 * Setup Eventlisteners
	 * @returns
	 */
	function init() {
		txtChatId.addEventListener("change", _chatIdChanged, false);
		btnClearCombobox.addEventListener("click", _clearComboBox, false);
		btnSend.addEventListener("click", _sendChatMessage, false);
	};

	/**
	 * Setup Eventlisteners if DOM content fully loaded
	 * @returns
	 */
	init();
});