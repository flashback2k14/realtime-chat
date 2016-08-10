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
	// Combobox - Chat ID
	var fiContainerId = document.querySelector("#fiContainerId");
	var cboChatId = document.querySelector("#cboChatId");
	var ulChatIds = document.querySelector("#ulChatIds");
	// Open Show Dialog
	var btnAddChatId = document.querySelector("#btnAddChatId");
	// Clear Chat
	var btnClearCombobox = document.querySelector("#btnClearCombobox");
	// Chat Name
	var txtChatName = document.querySelector("#txtChatName");
	// Chat Message
	var fiContainerMessage = document.querySelector("#fiContainerMessage");
	var txtChatMessage = document.querySelector("#txtChatMessage");
	// Send Chat Message
	var btnSend = document.querySelector("#btnSend");
	// Chat History
	var ulChatHistory = document.querySelector("#ulChatHistory");
	// Error Toast
	var errorToast = document.querySelector("#errorToast");
	// Dialog
	var addDialog = document.querySelector("#addDialog");
	var fiContainerNewId = document.querySelector("#fiContainerNewId");
	var txtDialogChatId = document.querySelector("#txtDialogChatId");
	var fiContainerNewDesc = document.querySelector("#fiContainerNewDesc");
	var txtDialogChatDescription = document.querySelector("#txtDialogChatDescription");
	var btnDialogSave = document.querySelector("#btnDialogSave");
	var btnDialogCancel = document.querySelector("#btnDialogCancel");

	var eventBus;
	
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
					if (xhr.status === 200 || xhr.status === 201) {
						if (xhr.responseText) {
							resolve(JSON.parse(xhr.responseText));
						} else {
							resolve();
						}
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
		_dataRequester("/api/chats/" + cboChatId.value)
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
	 * Register Event Bus to get Chat Messages
	 * @returns
	 */
	function _registerEventbusHandlerMessages() {
		if(eventBus !== undefined && eventBus !== null){
			eventBus.close();
		}
		eventBus = new EventBus(BASEURL + "/eventbus/messages");
		
		eventBus.onopen = function() {
			eventBus.registerHandler("chat." + cboChatId.value, function(error, message) {
				if (error) {
					_showErrorToast("Error: " + error.message);
					return;
				}
				var json = JSON.parse(message.body);
				if (json) {
					if (json.hasOwnProperty("error")) {
						_showErrorToast(json.error);
						return;
					} 
					if (json.hasOwnProperty("type")) {
						switch (json.type) {
							case "message":
								// add new List item to History
								_createListItem(json.response.author, json.response.content, json.response.created);
								break;
							case "chat":
								// add new List item to combobox
								_createComboboxItem(json.response.chatId);
								// reload combobox
								getmdlSelect.init(".getmdl-select");
								break;
							default:
								break;
						}
					}
				}
			});
		}
	};

	/**
	 * Register Event Bus to get Chat Ids
	 * @returns
	 */
	 function _registerEventbusHandlerIds() {
		 var eb = new EventBus(BASEURL + "/eventbus/chats");
			eb.onopen = function() {
				eb.registerHandler("cids", function(error, message) {
					if (error) {
						_showErrorToast("Error: " + error.message);
						return;
					}
					var json = JSON.parse(message.body);
					if (json) {
						if (json.hasOwnProperty("error")) {
							_showErrorToast(json.error);
							return;
						} 
						if (json.hasOwnProperty("type")) {
							switch (json.type) {
								case "message":
									// add new List item to History
									_createListItem(json.response.author, json.response.content, json.response.created);
									break;
								case "chat":
									// add new List item to combobox
									_createComboboxItem(json.response.chatId);
									// reload combobox
									getmdlSelect.init(".getmdl-select");
									break;
								default:
									break;
							}
						}
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
		cboChatId.value = "";
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
		_registerEventbusHandlerMessages();
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
		_dataRequester("/api/chats/" + cboChatId.value + "/messages", body)
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
	 * Create List item for Chat ID Combobox
	 * @param {any} msg Chat ID
	 * @returns
	 */
	function _createComboboxItem(msg) {
		var li = document.createElement("li");
		li.classList.add("mdl-menu__item");
		li.innerText = msg;
		ulChatIds.appendChild(li);
	};

	/**
	 * Get Chat IDs from server
	 * @returns
	 */
	function _getChatIds() {
		_dataRequester("/api/chats")
			.then(function(response) {
				response.forEach(function(chatObj) {
					_createComboboxItem(chatObj.chatId);
				});
				getmdlSelect.init(".getmdl-select");
			})
			.catch(function(error) {
				_showErrorToast(error.message);
			});
	};

	/**
	 * Show Add Dialog
	 * @returns
	 */
	function _showAddChatIdDialog() {
		addDialog.showModal();
	};

	/**
	 * Clear Dialog Inputs
	 * @returns
	 */
	function _clearDialogInputs() {
		txtDialogChatId.value = "";
		txtDialogChatDescription.value = "";
		// remove classes to reset the layout
		fiContainerNewId.classList.remove("is-focused");
		fiContainerNewId.classList.remove("is-dirty");
		// remove classes to reset the layout
		fiContainerNewDesc.classList.remove("is-focused");
		fiContainerNewDesc.classList.remove("is-dirty");
	};

	/**
	 * Save new Chat
	 * @returns
	 */
	function _saveChat() {
		// get Inputs
		var newChatId = txtDialogChatId.value;
		var newChatDescription = txtDialogChatDescription.value;
		// check if new Chat ID is not empty
		if (newChatId.length <= 0) {
			_showErrorToast("Empty Chat Id is invalid!");
			return;
		}
		// check if new Chat Description is not empty
		if (newChatDescription.length <= 0) {
			_showErrorToast("Empty Chat Description is invalid!");
			return;
		}
		// create new Chat Object
		var body = {
			chatId: newChatId,
			chatDesc: newChatDescription
		};
		// send request to server
		_dataRequester("/api/chats", body)
			.then(function(response) {
				// show user notification
				_showErrorToast("New Chat was added!");
				// clear Dialog inputs
				_clearDialogInputs();
				// close Dialog
				addDialog.close();
			})
			.catch(function(error) {
				_showErrorToast(error.message);
			});
	};

	/**
	 * Close Add Dialog
	 * @returns
	 */
	function _cancelDialog() {
		addDialog.close();
	};

	/**
	 * Get Chat IDs and Setup Eventlisteners
	 * @returns
	 */
	function init() {
		// register Dialog
		if (!addDialog.showModal) {
			dialogPolyfill.registerDialog(addDialog);
		}
		// get Chat IDs
		_getChatIds();
//		register eventbus for new added Chats
		_registerEventbusHandlerIds();
		// Eventlisteners
		cboChatId.addEventListener("change", _chatIdChanged, false);
		btnAddChatId.addEventListener("click", _showAddChatIdDialog, false);
		btnDialogSave.addEventListener("click", _saveChat, false);
		btnDialogCancel.addEventListener("click", _cancelDialog, false);
		btnClearCombobox.addEventListener("click", _clearComboBox, false);
		btnSend.addEventListener("click", _sendChatMessage, false);
	};

	/**
	 * Setup Eventlisteners if DOM content fully loaded
	 * @returns
	 */
	init();
});