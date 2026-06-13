// File: src/data/mockData.js

export const categories = [
  "Hành động", "Phiêu lưu", "Hài hước", "Tình cảm",
  "Siêu nhiên", "Kinh dị", "Học đường", "Lịch sử"
];

export const trendingSeries = [
  {
    id: 1,
    title: "Vua Hải Tặc",
    author: "Eiichiro Oda",
    genres: ["Hành động", "Phiêu lưu", "Hài hước"],
    coverUrl: "https://placehold.co/200x280/ff7675/ffffff?text=One+Piece",
    description: "Câu chuyện về kỷ nguyên hải tặc vĩ đại. Cùng bình chọn cho chặng đường cuối cùng của Vua Hải Tặc!",
    status: "Published",
    chapters: [
      { id: 101, title: "Chương 1111: Tứ Hoàng Hành Động", votes: 340 },
      { id: 102, title: "Chương 1110: Tinh Tú Giáng Lâm", votes: 325 }
    ]
  },
  {
    id: 2,
    title: "Chú Thuật Hồi Chiến",
    author: "Gege Akutami",
    genres: ["Hành động", "Siêu nhiên", "Kinh dị"],
    coverUrl: "https://placehold.co/200x280/74b9ff/ffffff?text=JJK",
    description: "Cuộc chiến khốc liệt giữa các chú thuật sư. Ai sẽ là người sống sót cuối cùng?",
    status: "Reviewing",
    chapters: [
      { id: 201, title: "Chương 256: Hắc Thiểm", votes: 901 },
      { id: 202, title: "Chương 255: Quyết Chiến Trận Địa", votes: 850 }
    ]
  },
  {
    id: 3,
    title: "Thanh Gươm Diệt Quỷ",
    author: "Koyoharu Gotouge",
    genres: ["Hành động", "Lịch sử", "Siêu nhiên"],
    coverUrl: "https://placehold.co/200x280/55efc4/ffffff?text=Demon+Slayer",
    description: "Hành trình diệt quỷ bảo vệ em gái của Tanjirou. Hãy vote cho những phân cảnh cảm động nhất.",
    status: "Draft",
    chapters: [
      { id: 301, title: "Chương 205: Tương Lai Tuyệt Vời", votes: 219 },
      { id: 302, title: "Chương 204: Thế Giới Không Có Quỷ", votes: 198 }
    ]
  }
];