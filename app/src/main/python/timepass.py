import chess
import numpy as np

def get_valid_moves(fen, square):
    board = chess.Board(fen)
    moves = list(board.legal_moves)

    square_index = chess.SQUARE_NAMES.index(square)
    valid_moves = []

    for move in moves:
        if move.from_square == square_index:
            valid_moves.append(move.uci())
    return valid_moves

def make_move(fen, uci_move):
    board = chess.Board(fen)
    move = chess.Move.from_uci(uci_move)
    if move in board.legal_moves:
        board.push(move)
    return board.fen()

def fen_to_tensor(fen):
    board = chess.Board(fen)
    planes = np.zeros((14, 8, 8), dtype=np.float32)

    piece_map = {
        'P': 0, 'N': 1, 'B': 2, 'R': 3, 'Q': 4, 'K': 5,
        'p': 6, 'n': 7, 'b': 8, 'r': 9, 'q': 10, 'k': 11
    }

    for square in chess.SQUARES:
        piece = board.piece_at(square)
        if piece:
            row = 7 - (square // 8)
            col = square % 8
            planes[piece_map[piece.symbol()]][row][col] = 1.0

    planes[12][:, :] = int(board.turn == chess.WHITE)
    planes[13][:, :] = int(board.has_kingside_castling_rights(chess.WHITE))

    return planes[np.newaxis, :]

def get_all_moves(fen):
    board = chess.Board(fen)
    legal_moves = list(board.legal_moves)
    return [move.uci() for move in legal_moves]

def get_all_tens(fen):
    board = chess.Board(fen)
    legal_moves = list(board.legal_moves)

    all_tensors = []

    for move in legal_moves:
        board.push(move)
        input_tensor = fen_to_tensor(board.fen())  # should return shape (14, 8, 8)
        all_tensors.append(input_tensor)
        board.pop()

    return np.array(all_tensors)  # shape: (num_moves, 14, 8, 8)

def get_all_tensor(fen):
    board = chess.Board(fen)
    move_tensor_pairs = []

    for move in board.legal_moves:
        board.push(move)
        tensor = fen_to_tensor(board.fen()).flatten().tolist()
        move_tensor_pairs.append((move.uci(), tensor))
        board.pop()

    return move_tensor_pairs

def check_move(fen):
    board = chess.Board(fen)
    if board.turn == chess.WHITE:
        return True
    return False

def is_pawn_at(fen, position):
    board = chess.Board(fen)
    square = chess.parse_square(position)
    piece = board.piece_at(square)

    return piece is not None and piece.piece_type == chess.PAWN

def is_game_over(fen):
    board = chess.Board(fen)
    return board.is_checkmate()
